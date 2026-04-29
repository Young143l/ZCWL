## /prjectMCP

### POST /ask/:project

#### 描述

询问该项目相关,project为根目录下的文件夹名，一个文件夹便是一个项目

#### 接口

```http
POST /ask/:id
"Content-Type": "application/json"
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

  ```json
  {
      "ans":"",
  }
  ```
  
  

### GET /doc/:project

#### 描述

生成该项目相关的学习分析文档。

#### 接口

```http
GET /doc/:id
"Content-Type": "application/json"
```

#### 响应

- 成功（200 OK）

  ```json
  {
      "doc":""
  }
  ```
