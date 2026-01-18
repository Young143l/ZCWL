# Spring Boot 项目说明文档

## 1. 项目概述

这是一个基于 Spring Boot 2.7.18 开发的后端 API 服务，用于管理用户、文档、标签、对话和问答等数据。项目使用 PostgreSQL 数据库，并提供了完整的 RESTful API 接口供前端调用。

### 1.1 项目背景

随着企业信息化建设的不断深入，文档管理、知识管理成为企业运营的重要组成部分。本项目旨在提供一个高效、安全的后端服务，支持文档的存储、检索、标签管理，以及用户与系统之间的交互功能。

### 1.2 项目目标

- 提供完整的 RESTful API 接口，支持前端应用的所有功能需求
- 实现用户认证与授权，确保系统安全
- 支持文档的结构化管理，包括文档内容、标签关联等
- 提供用户与系统的交互功能，包括对话和问答管理
- 支持用户个性化设置，如标签偏好管理

## 2. 技术栈说明

| 技术/框架               | 版本     | 用途                       |
|---------------------|--------|--------------------------|
| Java                | 14     | 后端开发语言                   |
| Spring Boot         | 2.7.18 | 后端框架，简化开发                |
| Spring Data JPA     | 2.7.18 | 数据库访问框架                  |
| PostgreSQL          | -      | 关系型数据库                   |
| Spring Security     | 2.7.18 | 安全框架，实现认证授权              |
| JWT                 | -      | JSON Web Token，用于无状态认证   |
| NoOpPasswordEncoder | -      | 密码编码器，使用明文密码存储（数据库表结构限制） |
| HikariCP            | -      | 数据库连接池                   |

## 3. 项目结构

项目采用分层架构，各层职责清晰，代码组织合理：

```
├── controller/     # 控制器层，处理 HTTP 请求，返回响应
│   ├── AuthController.java          # 认证管理控制器
│   ├── UserController.java          # 用户管理控制器
│   ├── DocController.java           # 文档管理控制器
│   ├── DocContentsController.java   # 文档内容控制器
│   ├── TagsController.java          # 标签管理控制器
│   ├── DocTagsController.java       # 文档标签关联控制器
│   ├── DialogController.java        # 对话管理控制器
│   ├── QueAnsController.java        # 问答管理控制器
│   └── UserTagPreferencesController # 用户标签偏好控制器
├── dto/            # 数据传输对象，用于前后端数据交互
│   ├── LoginRequestDTO.java         # 登录请求DTO
│   ├── LoginResponseDTO.java        # 登录响应DTO
│   └── UserDTO.java                # 用户DTO
├── entity/         # 实体类，对应数据库表
│   ├── User.java                    # 用户实体
│   ├── Doc.java                     # 文档实体
│   ├── DocContents.java             # 文档内容实体
│   ├── Tags.java                    # 标签实体
│   ├── DocTags.java                 # 文档标签关联实体
│   ├── Dialog.java                  # 对话实体
│   ├── QueAns.java                  # 问答实体
│   └── UserTagPreferences.java      # 用户标签偏好实体
├── repository/     # 仓库层，用于数据库操作
│   ├── UserRepository.java          # 用户仓库
│   ├── DocRepository.java           # 文档仓库
│   ├── DocContentsRepository.java   # 文档内容仓库
│   ├── TagsRepository.java          # 标签仓库
│   ├── DocTagsRepository.java       # 文档标签关联仓库
│   ├── DialogRepository.java        # 对话仓库
│   ├── QueAnsRepository.java        # 问答仓库
│   └── UserTagPreferencesRepository # 用户标签偏好仓库
├── service/        # 服务层，处理业务逻辑
│   ├── impl/        # 服务实现类
│   │   ├── AuthServiceImpl.java     # 认证服务实现
│   │   ├── UserServiceImpl.java     # 用户服务实现
│   │   ├── DocServiceImpl.java      # 文档服务实现
│   │   ├── DocContentsServiceImpl.java # 文档内容服务实现
│   │   ├── TagsServiceImpl.java     # 标签服务实现
│   │   ├── DocTagsServiceImpl.java  # 文档标签关联服务实现
│   │   ├── DialogServiceImpl.java   # 对话服务实现
│   │   ├── QueAnsServiceImpl.java   # 问答服务实现
│   │   └── UserTagPreferencesServiceImpl # 用户标签偏好服务实现
│   ├── AuthService.java             # 认证服务接口
│   ├── UserService.java             # 用户服务接口
│   ├── DocService.java              # 文档服务接口
│   ├── DocContentsService.java      # 文档内容服务接口
│   ├── TagsService.java             # 标签服务接口
│   ├── DocTagsService.java          # 文档标签关联服务接口
│   ├── DialogService.java           # 对话服务接口
│   ├── QueAnsService.java           # 问答服务接口
│   ├── UserTagPreferencesService.java # 用户标签偏好服务接口
│   └── UserDetailsServiceImpl.java  # Spring Security 用户详情服务
├── config/         # 配置类
│   ├── SecurityConfig.java          # Spring Security 配置
│   ├── JwtAuthenticationFilter.java # JWT 认证过滤器
│   ├── GlobalExceptionHandler.java  # 全局异常处理器
│   └── DataInitializer.java         # 数据初始化器
├── utils/          # 工具类
│   └── JwtTokenUtil.java            # JWT 令牌工具类
├── ZcwlApplication.java             # 应用主类
└── application.properties           # 应用配置文件
```

## 4. API 接口说明

### 4.1 认证管理 API

| API 路径                     | 请求方法 | 用途      | 前端调用示例         |
|----------------------------|------|---------|----------------|
| `/register`                | POST | 用户注册    | 新用户注册时调用       |
| `/`                        | POST | 用户登录    | 用户登录时调用        |
| `/validate-token`          | GET  | 验证token | 验证token是否有效时调用 |

#### 4.1.1 用户注册
- **请求 URL**: `http://localhost:8080/register`
- **请求方法**: POST
- **请求体**:
  ```json
  {
    "username": "张三",
    "password": "123456",
    "email": "zhangsan@example.com"
  }
  ```
- **成功响应** (201 Created):
  ```json
  {
    "uId": "张三",
    "name": "张三",
    "email": "zhangsan@example.com",
    "password": "123456"
  }
  ```
- **失败响应** (400 Bad Request):
  ```json
  "Username already exists"
  ```

#### 4.1.2 用户登录
- **请求 URL**: `http://localhost:8080/`
- **请求方法**: POST
- **请求体**:
  ```json
  {
    "userId": "张三",
    "password": "123456"
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "张三",
    "userId": "张三"
  }
  ```
- **失败响应** (401 Unauthorized):
  ```json
  "Invalid userId or password"
  ```

#### 4.1.3 验证token
- **请求 URL**: `http://localhost:8080/validate-token?token={token}`
- **请求方法**: GET
- **成功响应** (200 OK):
  ```json
  true
  ```
- **失败响应** (401 Unauthorized):
  ```json
  false
  ```

### 4.2 用户管理 API

| API 路径            | 请求方法   | 用途         | 前端调用示例       |
|-------------------|--------|------------|--------------|
| `/api/users`      | POST   | 创建用户       | 注册新用户时调用     |
| `/api/users/{id}` | GET    | 根据 ID 查询用户 | 查看特定用户信息时调用  |
| `/api/users`      | GET    | 查询所有用户（分页） | 管理员查看用户列表时调用 |
| `/api/users/{id}` | PUT    | 更新用户信息     | 修改用户资料时调用    |
| `/api/users/{id}` | DELETE | 删除用户       | 管理员删除用户时调用   |

#### 4.2.1 创建用户
- **请求 URL**: `http://localhost:8080/api/users`
- **请求方法**: POST
- **请求头**:
  ```
  Authorization: Bearer {token}
  ```
- **请求体**:
  ```json
  {
    "username": "李四",
    "email": "lisi@example.com",
    "password": "123456"
  }
  ```
- **成功响应** (201 Created):
  ```json
  {
    "uId": "李四",
    "name": "李四",
    "email": "lisi@example.com",
    "password": "123456"
  }
  ```

### 4.3 文档管理 API

| API 路径           | 请求方法   | 用途         |
|------------------|--------|------------|
| `/api/docs`      | POST   | 创建文档       |
| `/api/docs/{id}` | GET    | 根据 ID 查询文档 |
| `/api/docs`      | GET    | 查询所有文档（分页） |
| `/api/docs/{id}` | PUT    | 更新文档       |
| `/api/docs/{id}` | DELETE | 删除文档       |

### 4.4 文档内容 API

| API 路径                                  | 请求方法   | 用途                    |
|-----------------------------------------|--------|-----------------------|
| `/api/doc-contents`                     | POST   | 创建文档内容                |
| `/api/doc-contents/{docId}/{chapterId}` | GET    | 根据文档 ID 和章节 ID 查询文档内容 |
| `/api/doc-contents/doc/{docId}`         | GET    | 根据文档 ID 查询所有文档内容      |
| `/api/doc-contents`                     | GET    | 查询所有文档内容（分页）          |
| `/api/doc-contents/{docId}/{chapterId}` | PUT    | 更新文档内容                |
| `/api/doc-contents/{docId}/{chapterId}` | DELETE | 删除文档内容                |

### 4.5 标签管理 API

| API 路径           | 请求方法   | 用途         |
|------------------|--------|------------|
| `/api/tags`      | POST   | 创建标签       |
| `/api/tags/{id}` | GET    | 根据 ID 查询标签 |
| `/api/tags`      | GET    | 查询所有标签（分页） |
| `/api/tags/{id}` | PUT    | 更新标签       |
| `/api/tags/{id}` | DELETE | 删除标签       |

### 4.6 文档标签关系 API

| API 路径                          | 请求方法   | 用途                  |
|---------------------------------|--------|---------------------|
| `/api/doc-tags`                 | POST   | 创建文档标签关系            |
| `/api/doc-tags/{docId}/{tagId}` | GET    | 根据文档 ID 和标签 ID 查询关系 |
| `/api/doc-tags/doc/{docId}`     | GET    | 根据文档 ID 查询所有标签关系    |
| `/api/doc-tags/tag/{tagId}`     | GET    | 根据标签 ID 查询所有文档关系    |
| `/api/doc-tags`                 | GET    | 查询所有文档标签关系（分页）      |
| `/api/doc-tags/{docId}/{tagId}` | DELETE | 删除文档标签关系            |

### 4.7 用户标签偏好 API

| API 路径                                       | 请求方法   | 用途                  |
|----------------------------------------------|--------|---------------------|
| `/api/user-tag-preferences`                  | POST   | 创建用户标签偏好            |
| `/api/user-tag-preferences/{userId}/{tagId}` | GET    | 根据用户 ID 和标签 ID 查询偏好 |
| `/api/user-tag-preferences/user/{userId}`    | GET    | 根据用户 ID 查询所有标签偏好    |
| `/api/user-tag-preferences/tag/{tagId}`      | GET    | 根据标签 ID 查询所有用户偏好    |
| `/api/user-tag-preferences`                  | GET    | 查询所有用户标签偏好（分页）      |
| `/api/user-tag-preferences/{userId}/{tagId}` | PUT    | 更新用户标签偏好            |
| `/api/user-tag-preferences/{userId}/{tagId}` | DELETE | 删除用户标签偏好            |

### 4.8 对话管理 API

| API 路径                       | 请求方法   | 用途             |
|------------------------------|--------|----------------|
| `/api/dialogs`               | POST   | 创建对话           |
| `/api/dialogs/{id}`          | GET    | 根据 ID 查询对话     |
| `/api/dialogs/user/{userId}` | GET    | 根据用户 ID 查询所有对话 |
| `/api/dialogs`               | GET    | 查询所有对话（分页）     |
| `/api/dialogs/{id}`          | PUT    | 更新对话           |
| `/api/dialogs/{id}`          | DELETE | 删除对话           |

### 4.9 问答管理 API

| API 路径                            | 请求方法   | 用途                |
|-----------------------------------|--------|-------------------|
| `/api/que-ans`                    | POST   | 创建问答              |
| `/api/que-ans/{dialogId}/{times}` | GET    | 根据对话 ID 和问答次数查询问答 |
| `/api/que-ans/dialog/{dialogId}`  | GET    | 根据对话 ID 查询所有问答    |
| `/api/que-ans`                    | GET    | 查询所有问答（分页）        |
| `/api/que-ans/{dialogId}/{times}` | PUT    | 更新问答              |
| `/api/que-ans/{dialogId}/{times}` | DELETE | 删除问答              |

## 5. 前端如何调用 API

### 5.1 基本调用方式

前端可以使用任何 HTTP 客户端库（如 Axios、Fetch API 等）调用这些 API。以下是使用 Fetch API 调用的示例：

```javascript
// 创建用户示例
fetch('http://localhost:8080/api/users', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    username: '张三',
    email: 'zhangsan@example.com',
    password: '123456'
  }),
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

### 5.2 使用token进行认证

登录成功后，前端会获得一个JWT token，后续的API请求需要在请求头中携带这个token，才能访问需要认证的接口。

#### 5.2.1 登录并获取token

```javascript
// 登录示例
fetch('http://localhost:8080/', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    userId: '张三',
    password: '123456'
  }),
})
.then(response => {
  if (!response.ok) {
    throw new Error('登录失败');
  }
  return response.json();
})
.then(data => {
  // 保存token到本地存储
  localStorage.setItem('token', data.token);
  localStorage.setItem('username', data.username);
  localStorage.setItem('userId', data.userId);
  console.log('登录成功:', data);
  // 跳转到主页面
  window.location.href = '/home';
})
.catch(error => {
  console.error('登录失败:', error);
  // 显示错误信息给用户
  document.getElementById('error-message').textContent = '登录失败，请检查用户名和密码';
});
```

#### 5.2.2 携带token调用需要认证的接口

```javascript
// 携带token调用API示例
const token = localStorage.getItem('token');

if (!token) {
  // 没有token，跳转到登录页面
  window.location.href = '/login';
  return;
}

fetch('http://localhost:8080/api/users', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}` // 在请求头中携带token
  },
})
.then(response => {
  if (!response.ok) {
    // 处理401错误（token过期或无效）
    if (response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      // 跳转到登录页面
      window.location.href = '/login';
    }
    throw new Error('请求失败');
  }
  return response.json();
})
.then(data => {
  console.log('获取用户列表成功:', data);
  // 处理返回的数据，如显示用户列表
  renderUserList(data);
})
.catch(error => {
  console.error('请求失败:', error);
  // 显示错误信息给用户
  document.getElementById('error-message').textContent = '获取用户列表失败';
});
```

#### 5.2.3 处理token过期

前端应该在每次API调用后检查响应状态码，如果收到401 Unauthorized错误，说明token已经过期或无效，需要：
1. 清除本地存储中的token和用户信息
2. 跳转到登录页面，要求用户重新登录
3. 重新获取新的token

### 5.3 前端登录代码示例

以下是一个完整的前端登录页面代码示例，与后端API完全匹配：

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户登录</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .login-container {
            background-color: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            width: 350px;
        }
        h2 {
            text-align: center;
            margin-bottom: 30px;
            color: #333;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #555;
        }
        input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }
        input:focus {
            outline: none;
            border-color: #4CAF50;
            box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
        }
        .btn {
            width: 100%;
            padding: 12px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .btn:hover {
            background-color: #45a049;
        }
        .error-message {
            color: red;
            text-align: center;
            margin-top: 15px;
            padding: 10px;
            background-color: #ffebee;
            border-radius: 4px;
            display: none;
        }
        .register-link {
            text-align: center;
            margin-top: 20px;
        }
        .register-link a {
            color: #4CAF50;
            text-decoration: none;
        }
        .register-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>用户登录</h2>
        <form id="login-form">
            <div class="form-group">
                <label for="userId">用户名</label>
                <input type="text" id="userId" name="userId" required>
            </div>
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="btn">登录</button>
            <div id="error-message" class="error-message"></div>
        </form>
        <div class="register-link">
            还没有账号？<a href="register.html">立即注册</a>
        </div>
    </div>

    <script>
        document.getElementById('login-form').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const userId = document.getElementById('userId').value;
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('error-message');
            
            // 重置错误信息
            errorMessage.style.display = 'none';
            
            // 发送登录请求
            fetch('http://localhost:8080/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: userId,
                    password: password
                })
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.json();
            })
            .then(data => {
                // 登录成功，保存token并跳转到首页
                localStorage.setItem('token', data.token);
                localStorage.setItem('username', data.username);
                localStorage.setItem('userId', data.userId);
                window.location.href = 'index.html';
            })
            .catch(error => {
                // 显示错误信息
                errorMessage.textContent = error.message;
                errorMessage.style.display = 'block';
            });
        });
    </script>
</body>
</html>
```

### 5.4 注册页面代码示例

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>用户注册</title>
    <style>
        /* 与登录页面相同的样式 */
        body {
            font-family: Arial, sans-serif;
            background-color: #f0f2f5;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .login-container {
            background-color: white;
            padding: 40px;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            width: 350px;
        }
        h2 {
            text-align: center;
            margin-bottom: 30px;
            color: #333;
        }
        .form-group {
            margin-bottom: 20px;
        }
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #555;
        }
        input {
            width: 100%;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }
        input:focus {
            outline: none;
            border-color: #4CAF50;
            box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
        }
        .btn {
            width: 100%;
            padding: 12px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            font-weight: bold;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        .btn:hover {
            background-color: #45a049;
        }
        .error-message {
            color: red;
            text-align: center;
            margin-top: 15px;
            padding: 10px;
            background-color: #ffebee;
            border-radius: 4px;
            display: none;
        }
        .login-link {
            text-align: center;
            margin-top: 20px;
        }
        .login-link a {
            color: #4CAF50;
            text-decoration: none;
        }
        .login-link a:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2>用户注册</h2>
        <form id="register-form">
            <div class="form-group">
                <label for="username">用户名</label>
                <input type="text" id="username" name="username" required>
            </div>
            <div class="form-group">
                <label for="email">邮箱</label>
                <input type="email" id="email" name="email" required>
            </div>
            <div class="form-group">
                <label for="password">密码</label>
                <input type="password" id="password" name="password" required>
            </div>
            <button type="submit" class="btn">注册</button>
            <div id="error-message" class="error-message"></div>
        </form>
        <div class="login-link">
            已有账号？<a href="login.html">立即登录</a>
        </div>
    </div>

    <script>
        document.getElementById('register-form').addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('error-message');
            
            // 重置错误信息
            errorMessage.style.display = 'none';
            
            // 发送注册请求
            fetch('http://localhost:8080/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password
                })
            })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(text);
                    });
                }
                return response.json();
            })
            .then(data => {
                // 注册成功，跳转到登录页面
                alert('注册成功！请登录');
                window.location.href = 'login.html';
            })
            .catch(error => {
                // 显示错误信息
                errorMessage.textContent = error.message;
                errorMessage.style.display = 'block';
            });
        });
    </script>
</body>
</html>
```

### 5.5 请求方法说明

| 请求方法   | 用途   |
|--------|------|
| GET    | 获取数据 |
| POST   | 创建数据 |
| PUT    | 更新数据 |
| DELETE | 删除数据 |

### 5.6 HTTP 状态码说明

| 状态码 | 含义      | 处理方式              |
|-----|---------|-------------------|
| 200 | 请求成功    | 处理返回的数据           |
| 201 | 资源创建成功  | 处理返回的创建结果         |
| 204 | 资源删除成功  | 显示删除成功信息          |
| 400 | 请求参数错误  | 显示错误信息给用户，要求修正输入  |
| 401 | 未授权     | 清除本地token，跳转到登录页面 |
| 403 | 禁止访问    | 显示无权限提示           |
| 404 | 资源不存在   | 显示资源不存在提示         |
| 500 | 服务器内部错误 | 显示系统错误提示，建议稍后重试   |

## 6. 安全性说明

### 6.1 密码安全

- **密码存储**：使用 NoOpPasswordEncoder 存储明文密码（由于数据库表结构限制）
- **密码强度**：建议前端实现密码强度检查，要求密码长度至少6位
- **密码传输**：密码在传输过程中使用 HTTPS 加密，防止中间人攻击

### 6.2 JWT 认证

- **令牌生成**：使用安全的密钥生成 JWT 令牌，包含用户信息和过期时间
- **令牌验证**：每次请求都验证令牌的有效性，包括签名验证和过期检查
- **令牌存储**：前端将令牌存储在 localStorage 中，注意防止 XSS 攻击
- **令牌过期**：令牌设置合理的过期时间，过期后需要重新登录

### 6.3 输入验证

- **前端验证**：实现表单验证，确保用户输入的数据符合要求
- **后端验证**：使用 @Valid 注解和验证器，对所有输入参数进行验证
- **防止 SQL 注入**：使用 JPA 的参数化查询，防止 SQL 注入攻击
- **防止 XSS 攻击**：对用户输入的内容进行转义，防止 XSS 攻击

### 6.4 权限控制

- **基于角色的权限控制**：后续可以扩展实现基于角色的权限控制
- **API 访问控制**：使用 Spring Security 配置 API 的访问权限
- **敏感操作验证**：对敏感操作（如修改密码、删除用户等）进行额外验证

## 7. 错误处理

### 7.1 前端错误处理

前端应该对所有 API 请求进行错误处理，包括：
- 网络错误：显示网络连接失败提示
- 401 错误：跳转到登录页面
- 400 错误：显示参数错误提示
- 500 错误：显示系统错误提示

### 7.2 后端错误处理

后端使用全局异常处理器统一处理错误：
- **RuntimeException**：业务逻辑错误，返回 400 状态码和错误信息
- **MethodArgumentNotValidException**：参数验证错误，返回 400 状态码和错误信息
- **MissingServletRequestParameterException**：缺少请求参数错误，返回 400 状态码和错误信息
- **Exception**：其他错误，返回 500 状态码和通用错误信息

## 8. 数据库表关系

### 8.1 主要表结构

| 表名                   | 用途         |
|----------------------|------------|
| users                | 存储用户信息     |
| tags                 | 存储标签信息     |
| doc                  | 存储文档基本信息   |
| doc_contents         | 存储文档内容     |
| doc_tags             | 文档与标签的关联表  |
| dialog               | 存储对话信息     |
| que_ans              | 存储问答信息     |
| user_tag_preferences | 存储用户对标签的偏好 |

### 8.2 表关系图

```
users        ┌─────────┐        tags
┌─────┐      │ doc     │      ┌─────┐
│ u_id │─────┤┌─────┐  │─────┤ t_id │
│ name │     ││doc_id│  │     │ name │
│email │     │└─────┘  │     └─────┘
└─────┘      └─────────┘         │
     │                            │
     ▼                            ▼
┌────────────────┐        ┌──────────────────┐
│ dialog         │        │ user_tag_preferences │
│┌─────┐         │        │┌──────────────┐  │
││d_id │         │        ││(user_id,     │  │
│└─────┘         │        ││ tag_id)      │  │
└────────────────┘        └──────────────────┘
         │
         ▼
┌────────────────┐
│ que_ans        │
│┌────────────┐  │
││(d_id,      │  │
││ times)     │  │
└────────────────┘
```

## 9. 部署与维护

### 9.1 本地开发环境

#### 9.1.1 前置条件
- 安装 JDK 14 或更高版本
- 安装 Maven 3.6 或更高版本
- 安装 PostgreSQL 数据库

#### 9.1.2 启动步骤
1. 克隆项目代码
2. 修改 `application.properties` 文件中的数据库连接配置
3. 在项目根目录下执行命令：
   ```bash
   mvn spring-boot:run
   ```
4. 等待项目启动，看到 "Started ZcwlApplication" 表示启动成功
5. 项目默认运行在 `http://localhost:8080`

### 9.2 生产环境部署

#### 9.2.1 部署准备
- 准备生产环境服务器，安装 JDK 14
- 配置 PostgreSQL 数据库，创建相应的表结构
- 准备 HTTPS 证书，确保数据传输安全

#### 9.2.2 部署步骤
1. 执行 Maven 打包命令：
   ```bash
   mvn clean package
   ```
2. 将生成的 jar 文件上传到生产服务器
3. 配置环境变量，包括数据库连接信息、JWT 密钥等
4. 启动应用：
   ```bash
   java -jar zcwl-1.0.0.jar
   ```
5. 配置反向代理（如 Nginx），提供 HTTPS 访问

### 9.3 维护与监控

- **日志监控**：配置日志系统，监控应用运行状态
- **错误追踪**：实现错误追踪，及时发现和解决问题
- **性能监控**：监控应用性能，优化系统响应速度
- **备份策略**：定期备份数据库，防止数据丢失

## 10. 代码风格与规范

### 10.1 命名规范

- **包名**：使用小写字母，多级包名用点分隔
- **类名**：使用驼峰命名法，首字母大写
- **方法名**：使用驼峰命名法，首字母小写
- **变量名**：使用驼峰命名法，首字母小写
- **常量名**：使用大写字母，单词之间用下划线分隔

### 10.2 代码结构

- **类结构**：
  1. 包声明
  2. 导入语句
  3. 类注释
  4. 类声明
  5. 成员变量
  6. 构造函数
  7. 方法

- **方法结构**：
  1. 方法注释
  2. 方法声明
  3. 参数验证
  4. 业务逻辑
  5. 返回结果

### 10.3 注释规范

- **类注释**：每个类都要有详细的注释，说明类的用途、功能和设计思路
- **方法注释**：每个方法都要有详细的注释，说明方法的用途、参数、返回值和异常
- **参数注释**：对方法的每个参数进行注释，说明参数的含义和要求
- **返回值注释**：对方法的返回值进行注释，说明返回值的含义
- **异常注释**：对方法可能抛出的异常进行注释，说明异常的原因

### 10.4 代码风格

- **缩进**：使用 4 个空格进行缩进
- **换行**：每行代码长度不超过 120 个字符，超过需要换行
- **空行**：在逻辑块之间使用空行分隔，提高代码可读性
- **括号**：使用大括号包围所有代码块，包括单行代码
- **空格**：在操作符两侧、逗号后、分号后使用空格

## 11. 学习建议

### 11.1 后端学习路径

1. **基础学习**：
   - Java 基础语法
   - Spring Boot 核心概念
   - Spring Data JPA 数据库操作

2. **进阶学习**：
   - RESTful API 设计
   - Spring Security 安全框架
   - JWT 认证机制
   - 异常处理与日志

3. **实践项目**：
   - 实现完整的 CRUD 操作
   - 集成第三方服务
   - 性能优化与调优

### 11.2 前端学习建议

1. **基础学习**：
   - HTML/CSS/JavaScript 基础
   - 前端框架（如 React、Vue 等）
   - HTTP 请求与响应

2. **API 调用**：
   - 使用 Fetch API 或 Axios 调用后端 API
   - 实现登录认证流程
   - 处理 API 响应和错误

3. **用户体验**：
   - 实现友好的表单验证
   - 优化页面加载速度
   - 实现响应式设计

### 11.3 调试技巧

- **后端调试**：
  - 使用 IDE 的调试功能设置断点
  - 查看日志输出，了解系统运行状态
  - 使用 Postman 测试 API 接口

- **前端调试**：
  - 使用浏览器的开发者工具
  - 查看网络请求和响应
  - 使用 console.log 输出调试信息

## 12. 常见问题解答

### Q1: 为什么 Swagger 无法访问？

A: 目前 Swagger 已禁用，因为 Spring Boot 2.7.18 与 Springfox 3.0.0 存在兼容性问题。您可以通过查看代码中的注释了解 API 详情，或使用 SpringDoc OpenAPI 替代 Springfox。

### Q2: 如何修改数据库连接配置？

A: 在 `src/main/resources/application.properties` 文件中修改数据库连接配置：

```properties
# 数据库配置
spring.datasource.url=jdbc:postgresql://young143.top:5432/zcwl
spring.datasource.username=zcwl
spring.datasource.password=DbJhS5Jw8jcXp4CC
```

### Q3: 如何修改服务器端口？

A: 在 `application.properties` 文件中修改 `server.port` 配置：

```properties
server.port=8080
```

### Q4: 如何修改 JWT 令牌过期时间？

A: 在 `application.properties` 文件中修改 `jwt.expire-time` 配置：

```properties
# JWT配置
jwt.expire-time=86400000
```

### Q5: 如何处理跨域请求？

A: 项目已经配置了 CORS 支持，允许所有来源的跨域请求。如果需要修改 CORS 配置，可以在 `SecurityConfig.java` 文件中修改 `corsConfigurationSource` 方法。

### Q6: 如何在另一台电脑上访问后端服务？

A: 后端服务已经配置为允许外部访问，您需要：
1. 确保后端服务运行在 `0.0.0.0:8080` 端口
2. 在前端代码中使用后端服务的 IP 地址或域名，例如：
   ```javascript
   // 假设后端服务运行在 IP 为 192.168.1.100 的电脑上
   fetch('http://192.168.1.100:8080/', {
     // 请求参数
   });
   ```
3. 确保两台电脑在同一局域网内，或者后端服务可以通过公网访问

### Q7: 为什么测试用户初始化失败？

A: 测试用户初始化代码已被注释掉，以防止启动时出现问题。如果需要启用测试用户初始化，请修改 `DataInitializer.java` 文件，取消注释相关代码。

### Q8: 如何确保密码安全？

A: 由于数据库表结构限制，当前使用 NoOpPasswordEncoder 存储明文密码。在生产环境中，建议：
1. 修改数据库表结构，增加密码字段长度
2. 使用 BCryptPasswordEncoder 进行密码加密存储
3. 实现 HTTPS 加密传输

## 13. 结语

本项目提供了一个完整的 Spring Boot 后端服务，包含用户认证、文档管理、标签管理、对话和问答管理等功能。项目采用分层架构，代码结构清晰，注释详细，非常适合初学者学习。

通过学习本项目，您可以掌握：
- Spring Boot 项目的搭建和配置
- RESTful API 的设计和实现
- Spring Security 和 JWT 认证
- 数据库操作和事务管理
- 异常处理和错误管理
- 前后端数据交互
- 跨域请求处理
- 外部应用访问配置

项目已经完全按照前端需求进行配置，包括：
- 实现了与前端登录代码匹配的认证 API
- 提供了完整的用户注册功能
- 配置了外部访问支持
- 禁用了测试用户初始化，确保系统安全性
- 保持了明文密码存储方式，与数据库表结构兼容
- 验证了前端登录代码的兼容性

希望本文档能够帮助您快速上手本项目，如有任何问题或建议，欢迎提出。祝您学习愉快！