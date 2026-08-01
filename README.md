# Nova — O2O 餐饮管理平台后端

一套完整的 **O2O 餐饮管理平台后端服务**，为管理后台（PC）、用户端（H5）和微信小程序提供统一 RESTful API。
覆盖员工管理、菜品/套餐管理、分类管理、购物车、订单流转、数据报表、工作台看板等完整业务链路。

---

## 功能特性

- **双端双令牌鉴权**：管理端 / 用户端使用独立 JWT 密钥签发 Token，无状态、可水平扩展
- **缓存优化**：Redis 缓存热点数据（分类 / 菜品 / 套餐列表），TTL 随机抖动防缓存雪崩，布隆过滤器防缓存穿透
- **登录安全**：管理端账号锁定（5 次失败锁 30 分钟）+ IP 限流（10 次/分钟），用户端 IP 限流
- **下单幂等**：基于 Redis 原子消费唯一令牌，防止订单重复提交
- **分布式定时任务**：Redis 分布式锁保证多实例部署下超时订单取消任务不重复执行
- **实时通知**：WebSocket 向管理端推送新订单消息
- **报表导出**：Apache POI 生成 Excel 营业数据报表
- **微信生态**：微信小程序登录（code2session）、手机号密码登录/注册
- **对象存储**：阿里云 OSS 图片上传 + 签名 URL
- **接口文档**：Knife4j（OpenAPI 3）在线文档自动生成

---

## 技术栈

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| 语言 | Java | 17 | — |
| 框架 | Spring Boot | 3.1.12 | 自动配置 + IoC + 嵌入式容器 |
| ORM | MyBatis + PageHelper | 3.0.3 / 2.1.0 | SQL 映射 + 自动物理分页 |
| 数据库 | MySQL | 8.0 | 主业务数据库（`sky_take_out`） |
| 缓存 | Redis | 7.x | 会话缓存 + 业务缓存 + 布隆过滤器 + 分布式锁 |
| 连接池 | Druid | 1.2.20 | 数据库连接池 + 监控 |
| 鉴权 | JWT (jjwt) | 0.12.6 | 双令牌无状态认证（管理端/用户端独立密钥） |
| 对象映射 | MapStruct | 1.5.5.Final | 编译期 DTO/Entity/VO 自动转换，零反射开销 |
| 接口文档 | Knife4j (Swagger) | 4.3.0 | OpenAPI 3 在线文档 |
| 文件存储 | 阿里云 OSS | 3.17.1 | 图片上传 + 签名 URL |
| 报表 | Apache POI | 5.2.5 | Excel 营业报表导出 |
| 实时通信 | WebSocket | 内置 | 管理端订单实时通知 |
| 定时任务 | Spring @Scheduled | — | 超时订单自动取消 |
| AOP | Spring AOP | — | 自动填充创建/更新字段 |
| 密码加密 | Spring Security Crypto | — | BCrypt 密码编码 |
| 部署 | Docker / docker-compose | — | 容器化一键编排 |

---

## 架构设计

```
客户端                         应用层（Spring Boot 3.1）              基础设施
┌──────────────┐      ┌──────────────────────────────────┐      ┌────────────┐
│ 管理后台(PC)  │ ──┐  │  Controller 层（参数校验/返回统一)│      │   MySQL 8   │
│ 用户端 H5     │ ──┤  │  Service 层（业务编排/事务/缓存) │────▶ │   Redis 7   │
│ 微信小程序    │ ──┴─▶│  Mapper 层（MyBatis 数据访问）   │      │  阿里云 OSS │
└──────────────┘  Nginx│  ─────────────────────────────── │      └────────────┘
                       │  横切关注点                       │
                       │  JWT 拦截器 / AOP 自动填充         │
                       │  全局异常 / WebSocket / 定时任务    │
                       └──────────────────────────────────┘
```

### 模块划分

| 模块 | 说明 |
|------|------|
| **nova-common** | 公共基础设施：常量、枚举、异常体系、统一响应模型、ThreadLocal 上下文、工具类（JWT/OSS/HTTP）、配置属性绑定 |
| **nova-pojo** | 数据对象：数据库实体（11 个）、DTO（16 个）、VO（6 个），不含业务逻辑 |
| **nova-server** | 核心业务服务：控制器、业务实现、Mapper、MapStruct 转换器、JWT 拦截器、AOP 切面、定时任务、WebSocket |

---

## 快速开始

### 环境要求

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0 |
| Redis | 7.x |

### 1. 初始化数据库与 Redis

确保本机已启动 MySQL 和 Redis，并创建数据库 `sky_take_out`。连接信息在 `application-dev.yml`（本地开发，不入库）中配置，密码/密钥也可通过环境变量注入。

首次启动时会通过 `spring.sql.init` 自动执行 `schema.sql` 建表、`data.sql` 插入测试数据。

### 2. 启动后端

```bash
cd backEnd
mvn spring-boot:run -pl nova-server
```

启动后访问：

| 地址 | 说明 |
|------|------|
| http://localhost:8080/doc.html | Knife4j 在线接口文档 |
| http://localhost:8080/actuator/health | 健康检查 |
| http://localhost:8080/version | 版本信息 |

### 3. 测试账号

| 端 | 账号 | 密码 |
|----|------|------|
| 管理后台 | `admin` | `123456` |
| 用户 H5 | `13800138000` | `123456` |

---

## 配置说明

所有敏感配置均通过**环境变量**注入，未设置时使用默认值，生产环境必须显式配置。

### 必填环境变量

| 环境变量 | 说明 |
|----------|------|
| `JWT_ADMIN_SECRET` | 管理端 JWT 签名密钥（≥32 字符） |
| `JWT_USER_SECRET` | 用户端 JWT 签名密钥（≥32 字符） |
| `OSS_ENDPOINT` / `OSS_ACCESS_KEY_ID` / `OSS_ACCESS_KEY_SECRET` / `OSS_BUCKET_NAME` | 阿里云 OSS 凭证（不使用图片上传可留空） |

### 可选环境变量

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `WECHAT_APPID` / `WECHAT_SECRET` | 空 | 微信小程序登录凭证 |
| `CORS_ALLOWED_ORIGINS` | `*` | 允许的跨域来源，逗号分隔 |
| `MYSQL_ROOT_PASSWORD` | 必填（Docker 部署） | MySQL root 密码 |
| `REDIS_PASSWORD` | 必填（Docker 部署） | Redis 密码 |

---

## API 概览

详细接口参数请启动服务后访问 Knife4j 文档（http://localhost:8080/doc.html）。

### 鉴权方式

| 端 | 请求头 | 密钥 | 有效期 |
|----|--------|------|--------|
| 管理端 | `token` | `JWT_ADMIN_SECRET` | 2 小时 |
| 用户端 | `authentication` | `JWT_USER_SECRET` | 2 小时 |

### 调用示例

```bash
# 管理端登录
curl -X POST http://localhost:8080/admin/employee/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 带 Token 调用管理端接口
curl http://localhost:8080/admin/employee/page?page=1&pageSize=10 \
  -H "token: eyJhbGciOiJIUzI1NiJ9..."

# 用户端登录
curl -X POST http://localhost:8080/user/user/webLogin \
  -H "Content-Type: application/json" \
  -d '{"phone":"13800138000","password":"123456"}'

# 用户端查询分类（免鉴权）
curl http://localhost:8080/user/category/list?type=1
```

### 统一响应格式

```json
{ "code": 1, "data": { }, "msg": "操作成功" }
```

| 字段 | 说明 |
|------|------|
| `code` | 状态码，`1` 成功，`0` 失败 |
| `data` | 业务数据 |
| `msg` | 提示信息 |

---

## Docker 部署

### 1. 构建 JAR

```bash
cd backEnd
mvn clean package -DskipTests
```

### 2. 启动容器

```bash
cd backEnd/deploy
docker compose up -d
```

启动三个服务：

| 容器 | 端口映射 | 说明 |
|------|----------|------|
| `nova-mysql` | 3307:3306 | MySQL 8.0（首次启动自动执行建表/测试数据） |
| `nova-redis` | 6379:6379 | Redis（需设置 `REDIS_PASSWORD`） |
| `nova-server` | 8080:8080 | Spring Boot 应用 |

### 3. 环境变量文件

```bash
# deploy/.env
MYSQL_ROOT_PASSWORD=你的密码
REDIS_PASSWORD=你的密码
JWT_ADMIN_SECRET=管理端密钥(≥32字符)
JWT_USER_SECRET=用户端密钥(≥32字符)
OSS_ENDPOINT=你的OSS端点
OSS_ACCESS_KEY_ID=你的AK
OSS_ACCESS_KEY_SECRET=你的SK
OSS_BUCKET_NAME=你的桶名
CORS_ALLOWED_ORIGINS=*
```

---

## 前端项目

| 项目 | 技术栈 | 说明 |
|------|--------|------|
| `frontEnd/nova-admin` | Vue 3 + Element Plus | PC 管理后台 |
| `frontEnd/nova-user` | Vue 3 + Vant | 移动端 H5 用户端 |
| `frontEnd/sky-miniapp` | 微信原生小程序 | 微信小程序端 |

---

## 项目目录

```
backEnd/
├── pom.xml                      # 父 POM（多模块聚合）
├── nova-common/                 # 公共基础设施模块
│   └── src/main/java/com/nova/
│       ├── constant/            # 常量（JWT Claims、OSS 目录、消息、状态码）
│       ├── context/             # ThreadLocal 请求上下文（当前用户 ID）
│       ├── enumeration/         # 枚举（OperationType、OrderStatus）
│       ├── exception/           # 自定义异常体系
│       ├── json/                # Jackson 自定义序列化配置
│       ├── properties/          # 配置属性绑定（OSS/JWT/微信）
│       ├── result/              # 统一响应模型（Result<T>、PageResult）
│       └── utils/               # 工具类（JwtUtil、AliOssUtil、HttpClientUtil）
├── nova-pojo/                   # 数据对象模块（无业务逻辑）
│   └── src/main/java/com/nova/
│       ├── entity/              # 数据库实体
│       ├── dto/                 # 数据传输对象
│       └── vo/                  # 视图对象
├── nova-server/                 # 业务服务模块（核心代码）
│   └── src/main/java/com/nova/
│       ├── NovaApplication.java # 启动入口
│       ├── controller/          # REST 控制器（admin / user / common）
│       ├── service/             # 业务接口 + 实现
│       ├── mapper/              # MyBatis Mapper
│       ├── converter/           # MapStruct 对象转换
│       ├── interceptor/         # JWT 鉴权拦截器
│       ├── handler/             # 全局异常处理器
│       ├── config/              # Spring 配置类（Redis/OSS/WebSocket/布隆过滤器）
│       ├── aspect/              # AOP 切面（自动填充）
│       ├── annotation/          # 自定义注解（@AutoFill）
│       ├── task/                # 定时任务（超时订单取消）
│       └── websocket/           # WebSocket 服务端（订单通知）
│   └── src/main/resources/
│       ├── application.yml      # 主配置
│       ├── application-dev.yml  # 开发环境敏感配置（不入库）
│       └── mapper/              # MyBatis XML 映射
└── deploy/                      # 部署配置
    ├── Dockerfile
    ├── docker-compose.yml
    └── sql/                     # 建表 + 测试数据
```

---

## License

MIT
