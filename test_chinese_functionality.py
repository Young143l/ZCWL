import requests
import json

# 测试基础URL
BASE_URL = "http://localhost:8080"

# 测试中文用户注册
def test_register_chinese_user():
    print("\n=== 测试中文用户注册 ===")
    url = f"{BASE_URL}/users"
    headers = {"Content-Type": "application/json"}
    
    # 测试用例1：正常中文用户名注册
    print("测试用例1：正常中文用户名注册")
    data = {
        "userName": "新的中文用户456",
        "password": "Password123!",
        "email": "test1@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2：中文用户名长度边界测试（最小长度）
    print("\n测试用例2：中文用户名长度边界测试（最小长度）")
    data = {
        "userName": "中文123",
        "password": "Password123!",
        "email": "test2@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例3：中文用户名长度边界测试（最大长度）
    print("\n测试用例3：中文用户名长度边界测试（最大长度）")
    data = {
        "userName": "中文测试用户12345678901234567",
        "password": "Password123!",
        "email": "test3@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例4：中文用户名包含特殊字符
    print("\n测试用例4：中文用户名包含特殊字符")
    data = {
        "userName": "中文测试@用户",
        "password": "Password123!",
        "email": "test4@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试中文用户登录
def test_login_chinese_user():
    print("\n=== 测试中文用户登录 ===")
    url = f"{BASE_URL}/users/login"
    headers = {"Content-Type": "application/json"}
    
    # 测试用例1：中文用户名正常登录
    print("测试用例1：中文用户名正常登录")
    data = {
        "userId": "新的中文用户123",
        "password": "Password123!"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2：中文用户名密码错误
    print("\n测试用例2：中文用户名密码错误")
    data = {
        "userId": "新的中文用户123",
        "password": "WrongPassword"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例3：不存在的中文用户名
    print("\n测试用例3：不存在的中文用户名")
    data = {
        "userId": "不存在的中文用户",
        "password": "Password123!"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试中文用户信息修改
def test_update_chinese_user_info():
    print("\n=== 测试中文用户信息修改 ===")
    # 使用测试token
    token = "valid-token"
    url = f"{BASE_URL}/users/testuser1"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    # 测试用例1：正常修改中文用户名
    print("测试用例1：正常修改中文用户名")
    data = {
        "name": "更新后的中文用户",
        "email": "updated@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2：中文用户名长度边界测试
    print("\n测试用例2：中文用户名长度边界测试")
    data = {
        "name": "中",
        "email": "updated@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试中文用户密码修改
def test_update_chinese_user_password():
    print("\n=== 测试中文用户密码修改 ===")
    # 使用测试token
    token = "valid-token"
    url = f"{BASE_URL}/users/password/testuser1"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    # 测试用例1：正常修改密码
    print("测试用例1：正常修改密码")
    data = {
        "password": "Password123!",
        "newPassword": "NewPassword123!"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2：新密码与中文用户名相同
    print("\n测试用例2：新密码与中文用户名相同")
    data = {
        "password": "NewPassword123!",
        "newPassword": "中文测试用户123"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试文档管理模块的中文处理
def test_document_chinese():
    print("\n=== 测试文档管理模块的中文处理 ===")
    # 使用测试token
    token = "valid-token"
    url = f"{BASE_URL}/doc"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    # 测试用例：创建中文标题文档
    print("测试用例：创建中文标题文档")
    data = {
        "docName": "中文文档标题",
        "summary": "中文文档摘要",
        "icon": "icon.png",
        "userId": "testuser1"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试标签管理模块的中文处理
def test_tag_chinese():
    print("\n=== 测试标签管理模块的中文处理 ===")
    # 使用测试token
    token = "valid-token"
    url = f"{BASE_URL}/tags"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    # 测试用例：创建中文标签
    print("测试用例：创建中文标签")
    data = {
        "name": "中文标签"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

# 测试对话管理模块的中文处理
def test_dialog_chinese():
    print("\n=== 测试对话管理模块的中文处理 ===")
    # 使用测试token
    token = "valid-token"
    url = f"{BASE_URL}/dialogs"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    #// 测试用例：发送中文消息
    print("测试用例：发送中文消息")
    data = {
        "user": {
            "uId": "testuser1"
        },
        "qaTimes": 0,
        "dAbstract": "这是一条中文消息"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    # 运行所有测试
    test_register_chinese_user()
    test_login_chinese_user()
    test_update_chinese_user_info()
    test_update_chinese_user_password()
    test_document_chinese()
    test_tag_chinese()
    test_dialog_chinese()
    print("\n=== 测试完成 ===")
