## /users

### POST /users/login

#### 功能

登录使用接口

#### 接口

``` http
POST /users/login
"Content-Type": "application/json"
{
	userId: "userId",
	password: "password"
}
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "userName": "name",
      "userId": "id",
     	"token": "token"。
      "email": "email"
  }
  ```

- 失败（401  Unauthorized）

  ```json
  {
  }
  ```

### GET /users/:id

#### 功能

获取用户信息接口

#### 接口

```http
GET /users/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK)

  ```json
  {
      "name":"name",
    	"email":"email"
  }
  ```
  
- 验证失败（401  Unauthorized）

- 信息不存在（404 Not Found）


### POST /users/:id

#### 描述

修改用户信息

#### 接口

```http
POST /users/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
    "name":"name",
  	"email":"email"
}
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "name":"name",
    	"email":"email"
  }
  ```

### POST /users/password/:id

#### 描述

修改密码

#### 接口

```http
POST /users/password/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
    "password":"",
    "newPassword":"",
}
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "password":"",
  }
  ```

- 失败（原密码错误，新密码格式前端已校验，大概不会出错）

  ```json
  {
      "message":""
  }
  ```
  
  

## /doc

### GET /doc

#### 接口

```http
GET /doc
"Content-Type": "application/json"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "docList":[
          {
              "id":" id",
      		"name": "name",
      		"summary": "sum",
      		"img": "https:....."
          }
      ]
  }
  ```

- 失败（404 Not Found）

### GET /doc/:d_id

#### 接口

```http
GET /doc/:d_id
"Content-Type": "application/json"
```

#### 响应

- 成功（200 OK）
    ```json
    {
        "docInfo":{
            "id":" id",
            "name": "name",
            "summary": "sum",
            "img": "https:....."
        },
        "docDir":[
            {
                "id":"id",
                "name":"name"
            },
        ]
    }
    ```

- 失败（404 Not Found）

### GET /doc/:d_id/:c_id

#### 接口

```http
GET /doc/:d_id/:c_id
"Content-Type": "application/json"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "d_id":"id",
      "c_id":"id",
      "title":"titile",
      "content":"content
  }
  ```

### /doc/comment/

#### GET /doc/comment/:d_id/:c_id

##### 描述

获取评论

##### 接口

```http
GET /doc/:d_id/:c_id
"Content-Type": "application/json"
```

##### 响应

- 成功（200 OK）

  ```json
  {
      "comments":[
          {
              "id":"",
              "uId":"",
              "email":"",
              "content":"",
         		"children":[
                  {
                      "id":""
                      "uId":"",
                      "email":"",
                      "content":"",
                  }
              ]
          },
      ]
  }
  ```

  

#### POST /doc/coment/:d_id/:c_id

##### 描述

提交评论

##### 接口

```http
POST /doc/coment/:d_id/:c_id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"uId":"",
	"email":"",
	"content":"",
	"fa":"", //父评论id，-1则表示无
}
```

##### 响应

- 成功（200 OK）

  ```json
  {
      "id":""
  }
  ```

  

## /aichatdoc

### GET /aichatdoc/:id

#### 接口

```http
GET /aichatdoc/:id
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "id":"",
      "chat":[
      	{
              "id":"",
              "ask":"",
              "ans":"",
    			"img":"",
          }
      ]
  }
  ```

### GET /aichatdoc/?u_id=

#### 接口

```http
GET /aichatdoc/?:u_id=
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "id":"",
      "u_id":"",
      "chat":[
      	{
              "id":"",
              "ask":"",
              "ans":"",
              "img":""
          }
      ]
  }
  ```



### POST /aichatdoc/:id

#### 描述

在指定id的对话创建询问

#### 接口

```http
POST /aichatdoc/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"ask":"",
	"u_id":"",
	"img":""
}
```

#### 响应

- 成功（200 OK)

  流式返回

### POST /aichatdoc/new

#### 描述

创建一个新的对话

#### 接口

```http
POST /aichatdoc/
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	“u_id":""
}
```

#### 响应

- 成功（200 OK)

  ```json
  {
      id=""，
  }
  ```

## /project

### GET /project/?u_id=“”

#### 描述

获取项目列表（可加筛选条件，指定用户）。

#### 接口

```http
GET /project/?u_id=""
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
  	projects:[
  		{
  			"proejctName":"",
  			"id":""
  		}
  	]
  }
  ```

### POST /project/

#### 描述

创建新项目

#### 接口

```http
POST /project/
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"uId":"",
	"projectName":"",
	"url":"",
}
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "id":"",
  }
  ```

### GET /project/:id

#### 描述

获取指定id项目的信息

#### 接口

```http
POST /project/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "id":"",
      "name":"",
      "path":{
          "floders":[
              {
                  "name":"",
                  "floders":[],
                  "files":[],
              },
          ],
          "files":["name1","name2"],
      }
  }
  ```


### POST /project/:id/

#### 描述

获取指定id项目的指定文件（fileName为带路径的文件名（如：/src/main.cpp））

#### 接口

```http
POST /project/:id/:fileName
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"fileName":""
}
```

#### 响应

- 成功（200 OK）

  ```json
  {
     	"fileName":"",
      "file":""
  }
  ```





### GET /project/delet/:id

#### 描述

删除指定id的项目

#### 接口

```http
GET /project/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json	
  {
      "id":"",
  }
  ```

  

### POST /project/ask/:id

#### 描述

询问该项目相关

#### 接口

```http
POST /project/ask/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"ask":"",
	"codeSnap":[
		{
			"fileName":"",
			"lineStart":1,
			"lineEnd":2,
		}
	]
}
```

#### 响应

- 成功（200 OK）

  流式返回

### GET /project/doc/:id

#### 描述

生成该项目相关的学习分析文档。

#### 接口

```http
GET /project/doc/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "doc":""
  }
  ```

  

## /code

### GET /code/?uId=”uid”

#### 描述

获取全部的代码生成项目的列表,可指定用户

#### 接口

```http
GET /code/
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  ```json
  {
  	"uId":"", //如果有
      "list":[
      	{
      		"name":"",
      		"id":"",
              "type":"",
  		}
      ]
      
  }
  ```

### /code/sf

#### POST /code/sf

##### 描述

创建新的简单前端代码生成项目

##### 接口

```http
POST /code/sf
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"uId":"",
	"projectName":"",
	"message":""
}
```

##### 响应

- 成功（200 OK)

  ```json
  {
      sfId:"",
      "code":{
          "html":"",
          "css":"",
          "javascript":""
      }
  }
  ```

#### POST /code/sf/:id

##### 描述

在id的项目中发起ai对话生成新的代码

##### 接口

```http
POST /code/sf/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"code":{
		"html":"",
		"css":"",
		"js":""
	}
	"message":"",
	"selectId":["",""]
}
```

##### 响应

- 成功（200 OK）

  ```json	
  {
      sfId:"",
      "code":{
          "html":"",
          "css":"",
          "javascript":""
      }
  }
  ```

  

#### GET /code/sf/:id

##### 描述

获取指定id的简易前端项目相关信息

##### 接口

```http
GET /code/sf/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

##### 响应

- 成功（200 OK）

  ```json
  {
      "sfId":"",
      "name":"",
      "code":{
          "html":"",
          "css":"",
          "javascript":""
      }
  }
  ```
  
  

#### GET /code/sf/delete/:id

##### 描述

删除指定sf项目

##### 接口

```http
GET /code/sf/delete/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

##### 响应

- 成功（200 OK)

  ```json
  {
      "id":""
  }
  ```

### /code/cp

#### POST code/cp

##### 描述

创建新的控制台应用代码生成项目

##### 接口

```http
POST /code/cp
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"uId":"",
	"type":"", //编程语言
	"projectName":"",
	"message":""
}
```

##### 响应

- 成功（200 OK)

  ```json
  {
     	"cpId":"",
      "code":"",
      "type":"" //编程语言
  }
  ```

#### POST code/cp/:id

##### 描述

在id的项目中发起ai对话生成新的代码

##### 接口

```http
POST /code/cp/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
{
	"code":"",
	"message":""
}
```

##### 响应

- 成功（200 OK）

  ```json	
  {
      "cpId":"",
      "code":""
  }
  ```


#### GET code/cp/:id

##### 描述

获取指定id的控制台项目相关信息

##### 接口

```http
GET /code/cp/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

##### 响应

- 成功（200 OK）

  ```json
  {
      "cpId":"",
      "name":"",
      "type":"", //编程语言
      "code":""
  }
  ```


#### GET code/cp/delete/:id

##### 描述

删除指定cp项目

##### 接口

```http
GET /code/cp/delete/:id
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

##### 响应

- 成功（200 OK)

  ```json
  {
      "id":""
  }
  ```

#### ws://ws/code/cp/:id?token=“”

#### 描述

websockt通讯，运行代码

#### 协议

json数据传输：

##### 后端：

```json
{
    "done":false,
    "message":""
}
```

```json
{
    "done":true，
    "message":"结束原因"
}
```

##### 前端：

```json
{
    "done":false,
    "input":""
}
```

```json
{
    "done":true //一般为用户手动停止运行
}
```





## /notification

### GET /notification/?uId=“”

#### 描述

获取用户未读评论消息

#### 接口

```http
GET /notification/?uId=""
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

```json
{
	"uId":"",
    "notifications":[
    	{
    		"nId":"",
    		"dId":"",
    		"cId":"",
    		"from":"",//哪个用户回复的
    		"time":"",
    		"content":""
		}
    ]
}
```

### GET /notification/:nId

#### 描述

已读某通知

#### 接口

```http
GET /notification/:nId
"Content-Type": "application/json"
"Authorization": "Bearer token"
```

#### 响应

- 成功（200 OK）

  无json返回

- 失败

  ```json
  {
      "message":""
  }
  ```

  
