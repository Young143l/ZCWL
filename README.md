# ZCWL 后端服务

ZCWL 是一个基于 Spring Boot 4.0.1 的现代化后端服务项目，集成了 AI 对话、文档管理、代码生成、RAG(检索增强生成) 等多种功能。

## 📋 项目信息

- **项目名称**: ZCWL (zcwl)
- **版本**: 1.0.0
- **Java 版本**: 23
- **框架**: Spring Boot 4.0.1
- **构建工具**: Maven
- **数据库**: PostgreSQL
- **缓存**: Redis

## 🚀 主要功能

### 核心功能模块

1. **用户认证与授权** (`AuthController`, `UserController`)
   - JWT Token 认证
   - 用户注册与登录
   - 用户信息管理

2. **AI 智能对话** (`AiChatController`, `DialogController`)
   - 集成通义千问 AI 模型 (qwen3-coder-480b-a35b-instruct)
   - 多轮对话支持
   - 聊天历史记录

3. **文档管理** (`DocController`, `DocContentsController`, `DocTagsController`)
   - 文档 CRUD 操作
   - 文档内容管理
   - 标签系统
   - 用户标签偏好

4. **RAG 检索增强生成** (`RagController`, `EmbeddingController`)
   - 向量数据库集成
   - 文档向量化 (text-embedding-v4)
   - 语义检索
   - 知识问答

5. **项目管理** (`ProjectController`)
   - 项目 CRUD
   - 项目分类管理

6. **代码生成** (`CodeController`)
   - 前端项目生成
   - Git 集成
   - 代码模板管理

7. **问答系统** (`QueAnsController`)
   - 问题管理
   - 答案管理
   - 知识库构建

## 🛠️ 技术栈

### 核心依赖

- **Spring Boot 4.0.1** - 核心框架
- **Spring Data JPA** - 数据持久层
- **Spring Security** - 安全认证
- **Spring WebSocket** - WebSocket 通信
- **Spring WebFlux** - 响应式编程
- **Spring Retry** - 重试机制

### 数据库与缓存

- **PostgreSQL** - 主数据库
- **Redis** - 缓存与会话管理
- **HikariCP** - 数据库连接池

### AI 与向量处理

- **OpenAI API** - AI 对话服务
- **阿里云 DashScope** - 向量嵌入服务
- **RAG** - 检索增强生成

### 安全与认证

- **JWT (JJWT)** - JSON Web Token 认证
- **Spring Security** - 安全配置

### 工具库

- **Lombok** - 简化 Java 代码
- **Guava** - Google 核心库 (限流)
- **Resilience4j** - 容错与限流
- **JGit** - Git 操作
- **Apache Commons IO** - 文件操作
- **七牛云 SDK** - 对象存储

### 日志与监控

- **Logback 1.5.26** - 日志框架
- **Spring Actuator** - 应用监控
- **Prometheus** - 指标收集

## 📦 项目结构

```
ZCWL_back_end/
├── src/main/
│   ├── java/com/example/zcwl/
│   │   ├── config/              # 配置类
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebConfig.java
│   │   │   └── WebSocketConfig.java
│   │   ├── controller/          # REST API 控制器
│   │   │   ├── AiChatController.java
│   │   │   ├── AuthController.java
│   │   │   ├── CodeController.java
│   │   │   ├── DialogController.java
│   │   │   ├── DocContentsController.java
│   │   │   ├── DocController.java
│   │   │   ├── DocTagsController.java
│   │   │   ├── EmbeddingController.java
│   │   │   ├── ProjectController.java
│   │   │   ├── QueAnsController.java
│   │   │   ├── RagController.java
│   │   │   ├── TagsController.java
│   │   │   ├── UserController.java
│   │   │   └── UserTagPreferencesController.java
│   │   ├── dto/                 # 数据传输对象
│   │   │   ├── LoginRequestDTO.java
│   │   │   ├── LoginResponseDTO.java
│   │   │   └── UserDTO.java
│   │   ├── entity/              # 实体类
│   │   │   ├── ChatHistory.java
│   │   │   ├── ConsoleProject.java
│   │   │   ├── Dialog.java
│   │   │   ├── Doc.java
│   │   │   ├── DocContents.java
│   │   │   ├── DocRag.java
│   │   │   ├── DocTags.java
│   │   │   ├── Project.java
│   │   │   ├── QueAns.java
│   │   │   ├── SimpleFrontendProject.java
│   │   │   ├── Tags.java
│   │   │   ├── User.java
│   │   │   └── UserTagPreferences.java
│   │   ├── exception/           # 异常处理
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── handler/             # WebSocket 处理器
│   │   │   └── ConsoleWebSocketHandler.java
│   │   ├── repository/          # 数据访问层
│   │   │   ├── ChatHistoryRepository.java
│   │   │   ├── ConsoleProjectRepository.java
│   │   │   ├── DialogRepository.java
│   │   │   ├── DocContentsRepository.java
│   │   │   ├── DocRagRepository.java
│   │   │   ├── DocRepository.java
│   │   │   ├── DocTagsRepository.java
│   │   │   ├── ProjectRepository.java
│   │   │   ├── QueAnsRepository.java
│   │   │   ├── SimpleFrontendProjectRepository.java
│   │   │   ├── TagsRepository.java
│   │   │   ├── UserRepository.java
│   │   │   └── UserTagPreferencesRepository.java
│   │   ├── service/             # 业务逻辑层
│   │   │   ├── impl/
│   │   │   │   ├── AiChatServiceImpl.java
│   │   │   │   ├── AuthServiceImpl.java
│   │   │   │   ├── ConsoleProjectServiceImpl.java
│   │   │   │   ├── DialogServiceImpl.java
│   │   │   │   ├── DocContentsServiceImpl.java
│   │   │   │   ├── DocServiceImpl.java
│   │   │   │   ├── DocTagsServiceImpl.java
│   │   │   │   ├── ProjectServiceImpl.java
│   │   │   │   ├── QueAnsServiceImpl.java
│   │   │   │   ├── RagServiceImpl.java
│   │   │   │   ├── SimpleFrontendProjectServiceImpl.java
│   │   │   │   ├── TagsServiceImpl.java
│   │   │   │   ├── UserDetailsServiceImpl.java
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   └── UserTagPreferencesServiceImpl.java
│   │   │   ├── AiChatService.java
│   │   │   ├── AuthService.java
│   │   │   ├── ConsoleProjectService.java
│   │   │   ├── ConsoleService.java
│   │   │   ├── DialogService.java
│   │   │   ├── DocContentsService.java
│   │   │   ├── DocService.java
│   │   │   ├── DocTagsService.java
│   │   │   ├── ProjectService.java
│   │   │   ├── QueAnsService.java
│   │   │   ├── RagService.java
│   │   │   ├── SimpleFrontendProjectService.java
│   │   │   ├── TagsService.java
│   │   │   ├── UserService.java
│   │   │   └── UserTagPreferencesService.java
│   │   ├── utils/               # 工具类
│   │   │   ├── JwtTokenUtil.java
│   │   │   └── QiniuUtil.java
│   │   └── ZcwlApplication.java # 启动类
│   └── resources/
│       ├── static/              # 静态资源
│       │   ├── console-app.html
│       │   └── console-test.html
│       └── application.properties # 应用配置
├── logs/                        # 日志目录
├── pom.xml                      # Maven 配置
└── README.md                    # 项目说明
```

## ⚙️ 配置说明

### 环境要求

- **JDK**: 23+
- **Maven**: 3.6+
- **PostgreSQL**: 12+
- **Redis**: 6+

### 关键配置项

#### 服务器配置
```properties
server.port=8080
server.address=0.0.0.0
```

#### 数据库配置
```properties
spring.datasource.url=jdbc:postgresql://young143.top:5432/zcwl
spring.datasource.username=zcwl
spring.datasource.password=<password>
```

#### Redis 配置
```properties
spring.data.redis.host=young143.top
spring.data.redis.port=6379
spring.data.redis.password=<password>
```

#### AI 模型配置
```properties
spring.ai.openai.api-key=<api-key>
spring.ai.openai.base-url=https://api.qnaigc.com/v1
spring.ai.openai.chat.options.model=qwen3-coder-480b-a35b-instruct
```

#### RAG 配置
```properties
rag.embedding.api-key=<api-key>
rag.embedding.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1
rag.embedding.model=text-embedding-v4
```

#### 七牛云配置
```properties
qiniu.access-key=<access-key>
qiniu.secret-key=<secret-key>
qiniu.bucket-name=zcwl-project
qiniu.domain=zcwl-project.young143.top
```

## 🔧 开发与运行

### 编译项目
```bash
mvn clean compile
```

### 打包项目
```bash
mvn clean package
```

### 运行应用
```bash
# 使用 Maven 运行
mvn spring-boot:run

# 或使用 JAR 包运行
java -jar target/zcwl-1.0.0.jar
```

### 测试
```bash
mvn test
```

## 📊 API 接口

### 认证相关
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册

### 用户管理
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息

### AI 对话
- `POST /ai/chat` - AI 对话
- `GET /ai/history` - 获取聊天历史

### 文档管理
- `GET /docs` - 获取文档列表
- `POST /docs` - 创建文档
- `PUT /docs/{id}` - 更新文档
- `DELETE /docs/{id}` - 删除文档

### RAG 检索
- `POST /rag/search` - 向量检索
- `POST /rag/embedding` - 文本向量化

### 代码生成
- `POST /code/generate` - 生成代码
- `GET /code/project/{id}` - 获取项目

### 项目管理
- `GET /projects` - 获取项目列表
- `POST /projects` - 创建项目

## 🔐 安全特性

- **JWT Token 认证**: 基于 Token 的无状态认证
- **Spring Security**: 细粒度的权限控制
- **密码加密**: 敏感信息加密存储
- **CORS 配置**: 跨域请求控制
- **输入验证**: 参数校验与过滤

## 📈 性能优化

- **虚拟线程**: 启用 Java 23 虚拟线程提高并发性能
- **连接池优化**: HikariCP 高性能连接池
- **Redis 缓存**: 热点数据缓存
- **懒加载**: 延迟初始化提高启动速度
- **重试机制**: Spring Retry 自动重试失败操作
- **限流保护**: Resilience4j 限流防止过载

## 📝 日志配置

- **日志框架**: Logback 1.5.26
- **日志级别**: INFO (可调整)
- **日志文件**: `logs/zcwl.log`
- **滚动策略**: 最大 10MB,保留 7 个历史文件

## 🔍 监控与运维

### Actuator 端点
- `/actuator/health` - 健康检查
- `/actuator/info` - 应用信息
- `/actuator/metrics` - 性能指标
- `/actuator/prometheus` - Prometheus 格式指标

### 健康检查
- 数据库连接检查
- Redis 连接检查
- 应用状态监控

## 📌 注意事项

1. **安全配置**: 生产环境请修改默认密钥和配置
2. **数据库迁移**: 首次运行需确保数据库已创建并配置正确
3. **API 密钥**: 所有第三方服务的 API 密钥需妥善保管
4. **日志管理**: 定期清理日志文件，避免占用过多磁盘空间
5. **连接池**: 根据实际负载调整连接池大小

## 🤝 开发团队

本项目为大学生创新创业训练计划项目

## 📄 许可证

Copyright © 2026 ZCWL Team

## 📞 联系方式

如有问题或建议，请联系项目开发团队。
