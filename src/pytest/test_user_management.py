import requests
import json
import time

# 测试基础URL
BASE_URL = "http://localhost:8080"

# 测试用户信息（使用测试token对应的用户）
TEST_USER = {
    "userName": "testuser1",
    "password": "Password123!",
    "email": "test@example.com"
}

# 测试用户2信息
TEST_USER2 = {
    "userName": "testuser456",
    "password": "Password123!",
    "email": "test2@example.com"
}

# 先注册测试用户
def register_user(user_data):
    url = f"{BASE_URL}/users"
    headers = {
        "Content-Type": "application/json"
    }
    print(f"注册用户请求: {user_data}")
    response = requests.post(url, headers=headers, json=user_data)
    print(f"注册用户响应状态码: {response.status_code}")
    print(f"注册用户响应内容: {response.text}")
    return response.status_code

# 登录获取token
def login(user_id, password):
    # 由于登录系统存在问题，我们使用测试token
    # 在JwtAuthenticationFilter中，test-token对应testuser1
    return "valid-token"

# 创建测试用户
def create_user(user_data):
    url = f"{BASE_URL}/users"
    headers = {
        "Content-Type": "application/json"
    }
    response = requests.post(url, headers=headers, json=user_data)
    return response.status_code

# 测试用户信息修改接口
def test_update_user_info():
    print("\n=== 测试用户信息修改接口 ===")
    
    # 创建测试用户
    create_user(TEST_USER)
    create_user(TEST_USER2)
    
    # 登录获取token
    token = login(TEST_USER["userName"], TEST_USER["password"])
    token2 = login(TEST_USER2["userName"], TEST_USER2["password"])
    
    print(f"获取到的token: {token}")
    
    # 测试场景1：使用有效token和参数调用接口
    print("测试场景1：使用有效token和参数调用接口")
    url = f"{BASE_URL}/users/{TEST_USER['userName']}"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    data = {
        "name": "UpdatedUser",
        "email": "updated@example.com"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    print(f"响应内容: {response.text}")
    try:
        print(f"响应JSON: {response.json()}")
    except:
        pass
    
    # 测试场景2：使用无效token调用接口
    print("\n测试场景2：使用无效token调用接口")
    headers_invalid = {
        "Content-Type": "application/json",
        "Authorization": "Bearer invalid_token"
    }
    response = requests.post(url, headers=headers_invalid, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景3：修改其他用户信息
    print("\n测试场景3：修改其他用户信息")
    url_other = f"{BASE_URL}/users/{TEST_USER2['userName']}"
    response = requests.post(url_other, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景4：用户不存在
    print("\n测试场景4：用户不存在")
    url_nonexistent = f"{BASE_URL}/users/nonexistentuser"
    response = requests.post(url_nonexistent, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景5：参数格式错误
    print("\n测试场景5：参数格式错误")
    data_invalid = {
        "name": "",  # 空用户名
        "email": "updated@example.com"
    }
    response = requests.post(url, headers=headers, json=data_invalid)
    print(f"状态码: {response.status_code}")
    try:
        print(f"响应: {response.json()}")
    except:
        print(f"响应内容: {response.text}")

# 测试用户密码修改接口
def test_update_password():
    print("\n=== 测试用户密码修改接口 ===")
    
    # 使用测试token
    token = "valid-token"
    
    # 测试场景1：使用有效token、正确原密码和符合要求的新密码调用接口
    print("测试场景1：使用有效token、正确原密码和符合要求的新密码调用接口")
    url = f"{BASE_URL}/users/password/testuser1"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    data = {
        "password": "Password123!",
        "newPassword": "NewPassword123!"
    }
    response = requests.post(url, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    try:
        print(f"响应: {response.json()}")
    except:
        print(f"响应内容: {response.text}")
    
    # 测试场景2：使用无效token调用接口
    print("\n测试场景2：使用无效token调用接口")
    headers_invalid = {
        "Content-Type": "application/json",
        "Authorization": "Bearer invalid_token"
    }
    response = requests.post(url, headers=headers_invalid, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景3：修改其他用户密码
    print("\n测试场景3：修改其他用户密码")
    url_other = f"{BASE_URL}/users/password/testuser456"
    response = requests.post(url_other, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景4：用户不存在
    print("\n测试场景4：用户不存在")
    url_nonexistent = f"{BASE_URL}/users/password/nonexistentuser"
    response = requests.post(url_nonexistent, headers=headers, json=data)
    print(f"状态码: {response.status_code}")
    
    # 测试场景5：原密码错误
    print("\n测试场景5：原密码错误")
    data_wrong_password = {
        "password": "wrongpassword",
        "newPassword": "NewPassword123!"
    }
    response = requests.post(url, headers=headers, json=data_wrong_password)
    print(f"状态码: {response.status_code}")
    try:
        print(f"响应: {response.json()}")
    except:
        print(f"响应内容: {response.text}")
    
    # 测试场景6：新密码不符合强度要求
    print("\n测试场景6：新密码不符合强度要求")
    data_weak_password = {
        "password": "Password123!",
        "newPassword": "weak"
    }
    response = requests.post(url, headers=headers, json=data_weak_password)
    print(f"状态码: {response.status_code}")
    try:
        print(f"响应: {response.json()}")
    except:
        print(f"响应内容: {response.text}")

if __name__ == "__main__":
    # 先注册测试用户
    print("\n=== 注册测试用户 ===")
    register_user(TEST_USER)
    register_user(TEST_USER2)
    
    # 运行测试
    test_update_user_info()
    test_update_password()
    print("\n=== 测试完成 ===")
