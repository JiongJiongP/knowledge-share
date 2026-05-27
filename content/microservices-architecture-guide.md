# 微服务架构设计实践指南

> **作者：** 张伟 &nbsp;|&nbsp; **日期：** 2026-05-10 &nbsp;|&nbsp; **类型：** 📊 PPT文件 &nbsp;|&nbsp; **标签：** `技术` `架构`

---

## 概述

从单体应用到微服务的演进并非一蹴而就。本文结合多个实际项目经验，系统梳理微服务架构设计的**核心决策点、常见陷阱**和**实践策略**。

![微服务架构概览](https://microservices.io/i/Microservices-Patterns.jpg)

---

## 1. 服务拆分策略

### 1.1 拆分原则

> 高内聚、低耦合 —— 把经常一起变化的东西放在一起。

| 策略 | 描述 | 适用场景 |
|------|------|----------|
| **按业务能力** | 每个服务对应一个业务领域 | 业务逻辑复杂，团队按业务线组织 |
| **按子域** | 依据 DDD 限界上下文拆分 | 有清晰领域模型的中大型项目 |
| **按非功能需求** | 按性能、安全、合规等要求隔离 | 特定模块有特殊 SLA 要求 |

### 1.2 拆分过程

```mermaid
graph LR
    A[单体应用] --> B[识别限界上下文]
    B --> C[提取公共服务]
    C --> D[逐步拆分核心域]
    D --> E[拆分支撑域]
    E --> F[持续优化边界]
```

### 1.3 何时不该拆

- 业务逻辑尚未稳定，频繁变更
- 团队规模小（< 10 人），通信开销大于收益
- 数据一致性要求极高（ACID 强依赖）
- 延迟敏感的实时交易系统

---

## 2. 通信协议选型

### 同步通信

```yaml
# REST API 设计规范示例
openapi: 3.0.0
info:
  title: Order Service API
  version: 1.0.0
paths:
  /orders:
    post:
      summary: 创建订单
      requestBody:
        content:
          application/json:
            schema:
              type: object
              properties:
                userId:
                  type: string
                items:
                  type: array
      responses:
        '201':
          description: 订单创建成功
        '400':
          description: 参数校验失败
```

### 异步通信

| 模式 | 中间件 | 适用场景 |
|------|--------|----------|
| **消息队列** | RabbitMQ | 业务解耦、削峰填谷 |
| **事件流** | Kafka | 日志采集、实时分析 |
| **事件总线** | Redis Stream | 轻量级事件驱动 |

```java
// 领域事件示例
public class OrderCreatedEvent {
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    // 幂等性保障
    private String eventId;  // UUID，消费者据此去重
}
```

---

## 3. 分布式事务处理

### 方案对比

| 方案 | 一致性 | 性能 | 复杂度 |
|------|--------|------|--------|
| **2PC / XA** | 强一致 | 低 | 中 |
| **Saga 编排** | 最终一致 | 高 | 高 |
| **TCC** | 强一致 | 中 | 高 |
| **本地消息表** | 最终一致 | 高 | 中 |
| **事件溯源** | 最终一致 | 极高 | 极高 |

### Saga 模式实现

```java
@Service
public class CreateOrderSaga {

    @Transactional
    public void execute(CreateOrderCommand cmd) {
        // Step 1: 创建订单 (本地事务)
        Order order = orderService.create(cmd);

        try {
            // Step 2: 扣减库存 (RPC + 补偿)
            inventoryService.deduct(cmd.getItems(), cmd.getOrderId());
        } catch (Exception e) {
            orderService.cancel(order.getId());
            throw new SagaException("库存扣减失败，订单已取消");
        }

        // Step 3: 创建支付单
        try {
            paymentService.create(order);
        } catch (Exception e) {
            inventoryService.restore(cmd.getItems(), cmd.getOrderId());
            orderService.cancel(order.getId());
            throw new SagaException("支付创建失败，已回滚");
        }
    }
}
```

---

## 4. 服务治理

### 4.1 服务发现

```
┌──────────────┐    注册    ┌──────────────┐
│  Service A   │ ────────→ │  Nacos/Consul│
│  (Provider)  │           │  (Registry)  │
└──────────────┘           └──────┬───────┘
                                  │ 订阅
                          ┌───────▼───────┐
                          │  Service B    │
                          │  (Consumer)   │
                          └───────────────┘
```

### 4.2 熔断降级

```java
@RestController
public class OrderController {

    @SentinelResource(
        value = "createOrder",
        fallback = "createOrderFallback",
        blockHandler = "createOrderBlockHandler"
    )
    @PostMapping("/orders")
    public Result<Order> create(@RequestBody CreateOrderRequest req) {
        return Result.ok(orderService.create(req));
    }

    // 业务异常降级
    public Result<Order> createOrderFallback(CreateOrderRequest req, Throwable t) {
        log.error("创建订单失败", t);
        return Result.fail(500, "服务暂时不可用，请稍后重试");
    }

    // 限流降级
    public Result<Order> createOrderBlockHandler(CreateOrderRequest req, BlockException e) {
        return Result.fail(429, "请求过于频繁，请稍后再试");
    }
}
```

---

## 5. 可观测性

### 三大支柱

| 支柱 | 工具 | 用途 |
|------|------|------|
| **日志 (Logging)** | ELK / Loki | 事件记录、故障排查 |
| **指标 (Metrics)** | Prometheus + Grafana | 性能监控、容量规划 |
| **链路追踪 (Tracing)** | Jaeger / SkyWalking | 调用链分析、性能瓶颈定位 |

### 埋点示例

```java
@RestController
public class ProductController {

    private final MeterRegistry meterRegistry;

    @GetMapping("/products/{id}")
    public Product getProduct(@PathVariable Long id) {
        // 业务指标
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Product product = productService.getById(id);
            // 记录成功
            meterRegistry.counter("product.query.success").increment();
            return product;
        } catch (Exception e) {
            // 记录失败
            meterRegistry.counter("product.query.failure").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("product.query.duration")
                .description("产品查询耗时")
                .register(meterRegistry));
        }
    }
}
```

---

## 6. 部署架构

```yaml
# docker-compose.yml 示例
services:
  gateway:
    image: nginx:alpine
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf

  order-service:
    build: ./order-service
    environment:
      - SPRING_PROFILES_ACTIVE=k8s
      - DB_HOST=mysql
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 512M

  inventory-service:
    build: ./inventory-service
    deploy:
      replicas: 2

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

---

## 总结

| 关注点 | 建议 |
|--------|------|
| **拆分粒度** | 先粗后细，2 Pizza Team 原则 |
| **通信方式** | 同步用 REST/gRPC，异步用消息队列 |
| **事务** | 尽量避免分布式事务，优先本地消息表 + 补偿 |
| **可观测性** | 从项目启动就建立，而非事后补救 |
| **部署** | 容器化 + CI/CD + 蓝绿部署 |

> 微服务不是银弹。引入微服务的同时，你也引入了一个分布式系统需要面对的所有复杂性。—— *Martin Fowler*
