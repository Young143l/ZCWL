# API 接口文档

## 认证方式

- JWT (JSON Web Token) 认证
- 认证成功后，返回的 `token` 需要在后续请求的 `Authorization` 请求头中携带，格式为：`Bearer {token}`

## 响应格式

### 成功响应

```json
{
  "status": "success",
  "data": {}
}
```

### 失败响应

```json
{
  "status": "error",
  "message": "错误信息"
}
```

## 状态码

| 状态码 | 描述           |
|-----|--------------|
| 200 | 请求成功         |
| 201 | 资源创建成功       |
| 204 | 资源删除成功，无内容返回 |
| 400 | 请求参数错误       |
| 401 | 未授权，认证失败     |
| 404 | 资源不存在        |
| 500 | 服务器内部错误      |

## 接口列表

### 1. 认证相关接口

#### 1.1 用户登录

**路径**: `POST /users/login`

**请求体**:

```json
{
  "userId": "user001",
  "password": "Password123!"
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "userName": "zhangsan",
  "userId": "user001",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

- 失败 (401 Unauthorized):

```json
{}
```

#### 1.2 用户注册

**路径**: `POST /register`

**请求体**:

```json
{
  "userName": "zhangsan",
  "password": "Password123!",
  "email": "zhangsan@example.com",
  "phone": "13800138000"
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "docId": 1,
  "uId": "zhangsan",
  "docName": "zhangsan",
  "email": "zhangsan@example.com",
  "phone": "13800138000",
  "password": "encrypted-password",
  "createTime": "2024-01-15T10:00:00",
  "updateTime": "2024-01-15T10:00:00"
}
```

- 失败 (400 Bad Request):

```json
"用户名已存在"
```

#### 1.3 验证 Token

**路径**: `GET /validate-token`

**请求参数**:

| 参数名   | 类型     | 描述        |
|-------|--------|-----------|
| token | string | JWT Token |

**响应**:

- 成功 (200 OK):

```json
true
```

- 失败 (401 Unauthorized):

```json
false
```

### 2. 用户相关接口

#### 2.1 创建用户

**路径**: `POST /users`

**请求体**:

```json
{
  "uId": "user001",
  "name": "张三",
  "email": "zhangsan@example.com",
  "password": "123456"
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "docId": 1,
  "uId": "user001",
  "docName": "张三",
  "email": "zhangsan@example.com",
  "phone": null,
  "password": "encrypted-password",
  "createTime": "2024-01-15T10:00:00",
  "updateTime": "2024-01-15T10:00:00"
}
```

#### 2.2 根据 ID 获取用户

**路径**: `GET /users/{id}`

**请求头**:

| 请求头           | 值              |
|---------------|----------------|
| Authorization | Bearer {token} |

**响应**:

- 成功 (200 OK):

```json
{
  "name": "zhangsan",
  "avatar": null,
  "email": "zhangsan@example.com"
}
```

- 失败 (404 Not Found):

```json
{}
```

### 3. 文档相关接口

#### 3.1 获取文档列表

**路径**: `GET /doc`

**响应**:

- 成功 (200 OK):

```json
{
  "docList": [
    {
      "id": 1,
      "name": "Spring Boot 指南",
      "summary": "Spring Boot 教程",
      "img": "https://example.com/img.png"
    }
  ]
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 3.2 获取文档详情

**路径**: `GET /doc/{d_id}`

**响应**:

- 成功 (200 OK):

```json
{
  "docInfo": {
    "id": 1,
    "name": "Spring Boot 指南",
    "summary": "Spring Boot 教程",
    "img": "https://example.com/img.png"
  },
  "docDir": []
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 3.3 获取文档内容

**路径**: `GET /doc/{d_id}/{c_id}`

**响应**:

- 成功 (200 OK):

```json
{
  "d_id": 1,
  "c_id": 2,
  "title": "第1章 介绍",
  "content": "这是第1章的内容..."
}
```

- 失败 (404 Not Found):

```json
{}
```

### 4. 文档内容管理接口

#### 4.1 创建文档内容

**路径**: `POST /api/doc-contents`

**请求体**:

```json
{
  "id": {
    "docId": 1,
    "chapterId": 1
  },
  "title": "第1章 介绍",
  "content": "这是第1章的内容..."
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "id": {
    "docId": 1,
    "chapterId": 1
  },
  "title": "第1章 介绍",
  "content": "这是第1章的内容..."
}
```

#### 4.2 获取文档内容

**路径**: `GET /api/doc-contents/{docId}/{chapterId}`

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "docId": 1,
    "chapterId": 1
  },
  "title": "第1章 介绍",
  "content": "这是第1章的内容..."
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 4.3 获取文档所有内容

**路径**: `GET /api/doc-contents/doc/{docId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "docId": 1,
      "chapterId": 1
    },
    "title": "第1章 介绍",
    "content": "这是第1章的内容..."
  }
]
```

#### 4.4 获取所有文档内容（分页）

**路径**: `GET /api/doc-contents/contents?page=0&size=10&sort=id.docId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                       |
|------|---------|--------------------------|
| page | integer | 页码，从0开始                  |
| size | integer | 每页显示的记录数                 |
| sort | string  | 排序字段和排序方式，例如id.docId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "id": {
        "docId": 1,
        "chapterId": 1
      },
      "title": "第1章 介绍",
      "content": "这是第1章的内容..."
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 4.5 更新文档内容

**路径**: `PUT /api/doc-contents/{docId}/{chapterId}`

**请求体**:

```json
{
  "title": "第1章 更新后的标题",
  "content": "更新后的内容..."
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "docId": 1,
    "chapterId": 1
  },
  "title": "第1章 更新后的标题",
  "content": "更新后的内容..."
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 4.6 删除文档内容

**路径**: `DELETE /api/doc-contents/{docId}/{chapterId}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 5. 对话相关接口

#### 5.1 创建对话

**路径**: `POST /api/dialogs`

**请求体**:

```json
{
  "user": {
    "uId": "user001"
  },
  "qaTimes": 0,
  "dAbstract": "这是一个新对话"
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "dId": 1,
  "user": {
    "uId": "user001"
  },
  "qaTimes": 0,
  "dAbstract": "这是一个新对话",
  "createTime": "2024-01-15T10:00:00"
}
```

#### 5.2 获取对话详情

**路径**: `GET /api/dialogs/{id}`

**响应**:

- 成功 (200 OK):

```json
{
  "dId": 1,
  "user": {
    "uId": "user001"
  },
  "qaTimes": 0,
  "dAbstract": "这是一个新对话",
  "createTime": "2024-01-15T10:00:00"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 5.3 获取用户对话

**路径**: `GET /api/dialogs/user/{userId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "dId": 1,
    "user": {
      "uId": "user001"
    },
    "qaTimes": 0,
    "dAbstract": "这是一个新对话",
    "createTime": "2024-01-15T10:00:00"
  }
]
```

#### 5.4 获取所有对话（分页）

**路径**: `GET /api/dialogs?page=0&size=10&sort=dId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                  |
|------|---------|---------------------|
| page | integer | 页码，从0开始             |
| size | integer | 每页显示的记录数            |
| sort | string  | 排序字段和排序方式，例如dId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "dId": 1,
      "user": {
        "uId": "user001"
      },
      "qaTimes": 0,
      "dAbstract": "这是一个新对话",
      "createTime": "2024-01-15T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 5.5 更新对话

**路径**: `PUT /api/dialogs/{id}`

**请求体**:

```json
{
  "qaTimes": 5,
  "dAbstract": "更新后的对话摘要"
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "dId": 1,
  "user": {
    "uId": "user001"
  },
  "qaTimes": 5,
  "dAbstract": "更新后的对话摘要",
  "createTime": "2024-01-15T10:00:00"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 5.6 删除对话

**路径**: `DELETE /api/dialogs/{id}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 6. 问答相关接口

#### 6.1 创建问答

**路径**: `POST /api/que-ans`

**请求体**:

```json
{
  "id": {
    "dId": 1,
    "times": 1
  },
  "que": "什么是 Spring Boot？",
  "ans": "Spring Boot 是一个用于快速开发 Spring 应用的框架",
  "date": "2024-01-15T10:00:00"
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "id": {
    "dId": 1,
    "times": 1
  },
  "que": "什么是 Spring Boot？",
  "ans": "Spring Boot 是一个用于快速开发 Spring 应用的框架",
  "date": "2024-01-15T10:00:00"
}
```

#### 6.2 获取单个问答

**路径**: `GET /api/que-ans/{dialogId}/{times}`

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "dId": 1,
    "times": 2
  },
  "que": "什么是 Spring Boot？",
  "ans": "Spring Boot 是一个用于快速开发 Spring 应用的框架",
  "date": "2024-01-15T10:00:00"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 6.3 获取对话问答

**路径**: `GET /api/que-ans/dialog/{dialogId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "dId": 1,
      "times": 1
    },
    "que": "什么是 Spring Boot？",
    "ans": "Spring Boot 是一个用于快速开发 Spring 应用的框架",
    "date": "2024-01-15T10:00:00"
  }
]
```

#### 6.4 获取所有问答（分页）

**路径**: `GET /api/que-ans?page=0&size=10&sort=id.dId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                     |
|------|---------|------------------------|
| page | integer | 页码，从0开始                |
| size | integer | 每页显示的记录数               |
| sort | string  | 排序字段和排序方式，例如id.dId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "id": {
        "dId": 1,
        "times": 1
      },
      "que": "什么是 Spring Boot？",
      "ans": "Spring Boot 是一个用于快速开发 Spring 应用的框架",
      "date": "2024-01-15T10:00:00"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 6.5 更新问答

**路径**: `PUT /api/que-ans/{dialogId}/{times}`

**请求体**:

```json
{
  "que": "更新后的问题？",
  "ans": "更新后的回答"
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "dId": 1,
    "times": 2
  },
  "que": "更新后的问题？",
  "ans": "更新后的回答",
  "date": "2024-01-15T10:00:00"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 6.6 删除问答

**路径**: `DELETE /api/que-ans/{dialogId}/{times}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 7. 标签相关接口

#### 7.1 创建标签

**路径**: `POST /api/tags`

**请求体**:

```json
{
  "name": "Spring Boot",
  "description": "Spring Boot相关标签"
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "tagId": 1,
  "name": "Spring Boot",
  "description": "Spring Boot相关标签"
}
```

#### 7.2 获取标签详情

**路径**: `GET /api/tags/{id}`

**响应**:

- 成功 (200 OK):

```json
{
  "tagId": 1,
  "name": "Spring Boot",
  "description": "Spring Boot相关标签"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 7.3 获取所有标签

**路径**: `GET /api/tags?page=0&size=10&sort=tagId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                    |
|------|---------|-----------------------|
| page | integer | 页码，从0开始               |
| size | integer | 每页显示的记录数              |
| sort | string  | 排序字段和排序方式，例如tagId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "tagId": 1,
      "name": "Spring Boot",
      "description": "Spring Boot 相关标签"
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 7.4 更新标签

**路径**: `PUT /api/tags/{id}`

**请求体**:

```json
{
  "name": "Spring Boot 2.7",
  "description": "Spring Boot 2.7相关标签"
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "tagId": 1,
  "name": "Spring Boot 2.7",
  "description": "Spring Boot 2.7相关标签"
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 7.5 删除标签

**路径**: `DELETE /api/tags/{id}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 8. 文档标签关系接口

#### 8.1 创建文档标签关系

**路径**: `POST /api/doc-tags`

**请求体**:

```json
{
  "id": {
    "docId": 1,
    "tagId": 1
  }
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "id": {
    "docId": 1,
    "tagId": 1
  }
}
```

#### 8.2 获取单个文档标签关系

**路径**: `GET /api/doc-tags/{docId}/{tagId}`

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "docId": 1,
    "tagId": 1
  }
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 8.3 获取文档的所有标签

**路径**: `GET /api/doc-tags/doc/{docId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "docId": 1,
      "tagId": 1
    }
  }
]
```

#### 8.4 获取标签的所有文档

**路径**: `GET /api/doc-tags/tag/{tagId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "docId": 1,
      "tagId": 1
    }
  }
]
```

#### 8.5 获取所有文档标签关系（分页）

**路径**: `GET /api/doc-tags?page=0&size=10&sort=id.docId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                       |
|------|---------|--------------------------|
| page | integer | 页码，从0开始                  |
| size | integer | 每页显示的记录数                 |
| sort | string  | 排序字段和排序方式，例如id.docId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "id": {
        "docId": 1,
        "tagId": 1
      }
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 8.6 删除文档标签关系

**路径**: `DELETE /api/doc-tags/{docId}/{tagId}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 9. 用户标签偏好接口

#### 9.1 创建用户标签偏好

**路径**: `POST /api/user-tag-preferences`

**请求体**:

```json
{
  "id": {
    "userId": "user001",
    "tagId": 1
  },
  "score": 5.5
}
```

**响应**:

- 成功 (201 Created):

```json
{
  "id": {
    "userId": "user001",
    "tagId": 1
  },
  "score": 5.5
}
```

#### 9.2 获取单个用户标签偏好

**路径**: `GET /api/user-tag-preferences/{userId}/{tagId}`

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "userId": "user001",
    "tagId": 1
  },
  "score": 5.5
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 9.3 获取用户的所有标签偏好

**路径**: `GET /api/user-tag-preferences/user/{userId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "userId": "user001",
      "tagId": 1
    },
    "score": 5.5
  }
]
```

#### 9.4 获取标签的所有用户偏好

**路径**: `GET /api/user-tag-preferences/tag/{tagId}`

**响应**:

- 成功 (200 OK):

```json
[
  {
    "id": {
      "userId": "user001",
      "tagId": 1
    },
    "score": 5.5
  }
]
```

#### 9.5 获取所有用户标签偏好（分页）

**路径**: `GET /api/user-tag-preferences?page=0&size=10&sort=id.userId,asc`

**请求参数**:

| 参数名  | 类型      | 描述                        |
|------|---------|---------------------------|
| page | integer | 页码，从0开始                   |
| size | integer | 每页显示的记录数                  |
| sort | string  | 排序字段和排序方式，例如id.userId,asc |

**响应**:

- 成功 (200 OK):

```json
{
  "content": [
    {
      "id": {
        "userId": "user001",
        "tagId": 1
      },
      "score": 5.5
    }
  ],
  "pageable": {
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalPages": 1,
  "totalElements": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

#### 9.6 更新用户标签偏好

**路径**: `PUT /api/user-tag-preferences/{userId}/{tagId}`

**请求体**:

```json
{
  "score": 7.8
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "id": {
    "userId": "user001",
    "tagId": 1
  },
  "score": 7.8
}
```

- 失败 (404 Not Found):

```json
{}
```

#### 9.7 删除用户标签偏好

**路径**: `DELETE /api/user-tag-preferences/{userId}/{tagId}`

**响应**:

- 成功 (204 No Content):

```json
```

- 失败 (404 Not Found):

```json
{}
```

### 10. AI 对话 API

#### 10.1 创建新对话

**路径**: `POST /aichatdoc`

**请求头**:

| 请求头           | 值                |
|---------------|------------------|
| Authorization | Bearer {token}   |
| Content-Type  | application/json |

**请求体**:

```json
{
  "u_id": "用户ID"
}
```

**响应**:

- 成功 (200 OK):

```json
{
  "id": "对话ID"
}
```

#### 10.2 发送消息到对话

**路径**: `POST /aichatdoc/{dialogId}`

**请求头**:

| 请求头           | 值                |
|---------------|------------------|
| Authorization | Bearer {token}   |
| Content-Type  | application/json |

**请求体**:

```json
{
  "ask": "用户问题",
  "u_id": "用户ID"
}
```

**响应**:

- 成功 (200 OK):
  - 流式响应，返回AI的回答内容
  - 示例响应（流式）:
    ```
    你好！很高兴你对学习C语言感兴趣。C语言是编程世界的重要基石，被誉为"编程的语言"。根据相关资料，学习C语言能帮你深刻理解计算机底层原理，为学习其他语言打下坚实基础，并极大提升逻辑思维能力。
    
    为了帮助你更好地学习C语言，我为你准备了一份详细的学习计划...
    ```
