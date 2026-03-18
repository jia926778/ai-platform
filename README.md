# AI 集成平台

基于 Spring Boot 3.2 和 Spring AI 构建的 AI 集成平台，提供智能对话、文本摘要、代码生成等 AI 能力。

## 技术栈

- **后端框架**: Spring Boot 3.2.4
- **AI 集成**: Spring AI 1.0.0-M4 (OpenAI)
- **安全认证**: Spring Security + JWT
- **数据库**: MySQL 8.0 + Spring Data JPA
- **缓存**: Redis
- **API 文档**: SpringDoc OpenAPI 2.3.0
- **构建工具**: Maven
- **Java 版本**: JDK 17

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 配置说明

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE ai_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 设置环境变量（或修改 `application.yml`）：
```bash
export DB_PASSWORD=your_mysql_password
export OPENAI_API_KEY=your_openai_api_key
export OPENAI_BASE_URL=https://api.openai.com
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=your_jwt_secret_key_at_least_32_characters
```

3. 启动应用：
```bash
mvn spring-boot:run
```

应用启动后访问：http://localhost:8080

API 文档地址：http://localhost:8080/swagger-ui.html

## API 概览

### 认证接口 `/api/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/refresh | 刷新令牌 |

### 对话接口 `/api/chat`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/chat/send | 发送消息 |
| GET | /api/chat/conversations | 获取对话列表 |
| GET | /api/chat/conversations/{id}/messages | 获取对话消息 |
| DELETE | /api/chat/conversations/{id} | 删除对话 |

### 摘要接口 `/api/summary`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/summary/generate | 生成文本摘要 |

### 代码生成接口 `/api/codegen`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/codegen/generate | 生成代码 |

### 监控接口 `/api/monitor`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/monitor/usage | 查看使用记录 |
| GET | /api/monitor/stats | 查看使用统计 |
| GET | /api/monitor/dashboard | 管理员仪表盘 |

### 用户接口 `/api/users`
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/users/me | 获取当前用户信息 |
| PUT | /api/users/me | 更新个人资料 |
| GET | /api/users | 获取用户列表（管理员） |
| PUT | /api/users/{id}/status | 更新用户状态（管理员） |

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

## 项目结构

```
src/main/java/com/example/aiplatform/
├── config/          # 配置类
├── controller/      # 控制器
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── exception/       # 异常处理
├── ratelimit/       # 限流组件
├── repository/      # 数据访问层
├── security/        # 安全认证
├── service/         # 业务逻辑
└── util/            # 工具类
```
