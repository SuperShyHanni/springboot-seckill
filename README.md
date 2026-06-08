# springboot-seckill 秒杀系统

模拟电商秒杀场景的后端系统，重点解决高并发下的三大问题：**超卖、性能瓶颈、流量冲击**。
通过「Redis 预扣减 + 数据库原子更新 + RabbitMQ 异步下单 + 令牌桶限流」组合，在保证库存绝不超卖的前提下，把数据库从高并发请求中保护起来。

## 技术栈

| 类别 | 技术 |
| --- | --- |
| 框架 | Spring Boot 3.3.13 (Java 17) |
| 持久层 | MyBatis 3.0.4 + MySQL |
| 缓存 | Redis (Spring Data Redis) |
| 消息队列 | RabbitMQ (Spring AMQP) |
| 认证 | JWT (jjwt 0.12.6) |
| 压测 | JMeter |

## 系统架构 / 核心流程

秒杀请求（以异步下单 `/seckill/async` 为例）从进入到生成订单的完整链路：

```
用户请求
   │
   ▼
① AuthInterceptor —— JWT 校验，解析出 userId 放入 UserContext
   │
   ▼
② RateLimiter —— 两层令牌桶限流（Lua 脚本在 Redis 中原子执行）
   │   · 全局限流 ratelimit:seckill        （1000 令牌/秒）
   │   · 单用户限流 ratelimit:user:{userId}（5 令牌/秒，防刷）
   │   限流未通过 → 直接返回「系统繁忙 / 操作过于频繁」
   ▼
③ Redis 预扣减 —— DECR seckill:stock:{goodsId}
   │   · 结果 < 0 → 库存已空，直接返回「已抢完」（绝大多数请求在这里被拦下，不碰数据库）
   ▼
④ 发送 MQ 消息 —— 把 (userId, goodsId) 投递到 seckill.queue，立即返回「排队中，请稍等」
   │
   ▼
⑤ SeckillConsumer 异步消费 —— 单线程/有限并发地慢慢落库
   │   · 重复下单校验：count(userId, goodsId) > 0 → 跳过
   │   · 数据库扣库存：UPDATE ... SET stock_count = stock_count-1 WHERE id=? AND stock_count > 0
   │   · 插入订单（唯一索引 uk_user_goods 兜底防重复）
   ▼
   订单生成完成
```

> 启动时 `StockWarmUp`（`CommandLineRunner`）会把数据库里每个商品的库存预热进 Redis（`seckill:stock:{id}`），所有扣减判断先在 Redis 完成。

项目同时保留了同步下单接口 `/seckill/do`：流程类似，但 Redis 预扣减后直接在请求线程里完成数据库扣减和落库，用于和异步方案做对比。

## 核心功能与设计取舍

### 1. 防止超卖
- **问题**：高并发下多个线程同时读到库存还剩 1，结果都减库存，库存被减成负数（超卖）。
- **方案**：库存扣减落到数据库的这一步用**原子条件更新**——
  `UPDATE goods SET stock_count = stock_count - 1 WHERE id = #{goodsId} AND stock_count > 0`，
  靠数据库行锁保证扣减原子性，更新影响行数为 0 就说明没库存了。
- **为什么这样而不是 `synchronized` / 悲观锁**：条件更新把「判断 + 扣减」合并成一条 SQL，并发安全且只锁单行，吞吐远高于在应用层加锁或 `SELECT ... FOR UPDATE`。

### 2. Redis 预扣减
- **问题**：如果每个请求都打到数据库查库存、扣库存，数据库扛不住秒杀瞬时流量。
- **方案**：库存启动时预热进 Redis，请求先用 `DECR` 在 Redis 里扣，扣完（结果 < 0）的请求直接被拦截返回，**根本不到数据库**。Redis 单线程执行 `DECR` 天然原子，不会扣超。
- **效果**：数据库只需要处理「Redis 判定还有库存」的那一小部分请求，起到第一道流量过滤。

### 3. 异步下单（RabbitMQ 削峰）
- **问题**：即使过了 Redis 那关，瞬间仍可能有大量合法请求要写数据库，写库速度跟不上请求速度。
- **方案**：Redis 预扣减成功后不直接写库，而是把下单消息投进 RabbitMQ，立即给用户返回「排队中」；消费者按数据库能承受的速度异步落库。这就是**削峰填谷**——把瞬时高峰摊平成平稳的写入流。
- **代价 / 现状**：用户拿到的是「排队中」而非最终结果，需要前端轮询「异步结果回查」接口确认是否下单成功（该接口为当前 TODO）。

### 4. 接口限流（令牌桶 + Lua）
- **方案**：基于 Redis 实现令牌桶限流，分全局和单用户两级。限流逻辑写成 Lua 脚本（`ratelimit.lua`），由 Redis **整段原子执行**。
- **为什么用 Lua 而不是几条 Redis 命令**：令牌桶要「读令牌数 → 按时间补令牌 → 判断够不够 → 写回」一连串操作，多条命令之间会被其他请求插队导致并发错误；Lua 脚本在 Redis 里不可中断地一次执行，保证整个判断的原子性。

## 快速开始

### 前置依赖
- JDK 17
- MySQL（建库 `seckill` 后执行 `sql/init.sql` 初始化表和测试数据）
- Redis
- RabbitMQ

### 配置
修改 `src/main/resources/application.yml` 中的数据库 / Redis / RabbitMQ 连接信息。

### 运行
```bash
mvn spring-boot:run
```
启动后访问 `http://localhost:8080/index.html`。

## 主要接口

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/user/login` | 登录，返回 JWT |
| GET | `/goods/list` | 商品列表 |
| POST | `/seckill/do` | 同步秒杀（请求线程内落库） |
| POST | `/seckill/async` | 异步秒杀（MQ 削峰，推荐） |

## 项目结构

```
src/main/java/com/lumos/seckill/
├── config/        # 拦截器(AuthInterceptor)、RabbitMQ 配置、库存预热(StockWarmUp)
├── controller/    # 接口层（商品、登录、秒杀）
├── service/       # 业务逻辑（SeckillService 核心、RateLimiter 限流、SeckillConsumer MQ 消费）
├── mapper/        # MyBatis 数据访问
├── entity/        # 实体类
└── util/          # JWT、MD5、用户上下文(UserContext)
src/main/resources/
├── lua/           # Redis Lua 脚本（令牌桶限流）
├── static/        # 前端页面
└── application.yml
sql/init.sql       # 建表 + 测试数据
seckill-do.jmx     # JMeter 压测脚本
```

## 后续计划
- [ ] 异步下单结果回查接口（前端轮询确认抢购结果）
- [ ] JMeter 压测与性能对比（同步 vs 异步）
- [ ] 缓存进一步完善
```
