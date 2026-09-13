import requests
import json

# 测试用户注册和登录的边界情况
def test_register_boundary_cases():
    print("测试用户注册边界情况...")
    
    # 测试用例1: 用户名长度小于6个字符
    print("\n测试1: 用户名长度小于6个字符")
    test_data = {
        "userName": "中文12",
        "password": "Test123!",
        "email": "test3@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2: 用户名长度超过20个字符
    print("\n测试2: 用户名长度超过20个字符")
    test_data = {
        "userName": "中文测试用户1234567890123",
        "password": "Test123!",
        "email": "test4@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例3: 用户名包含非法字符
    print("\n测试3: 用户名包含非法字符")
    test_data = {
        "userName": "中文测试@用户",
        "password": "Test123!",
        "email": "test5@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例4: 密码长度小于8个字符
    print("\n测试4: 密码长度小于8个字符")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test1!",
        "email": "test6@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例5: 密码长度超过20个字符
    print("\n测试5: 密码长度超过20个字符")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!Test123!Test123!",
        "email": "test7@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例6: 密码不包含大写字母
    print("\n测试6: 密码不包含大写字母")
    test_data = {
        "userName": "中文测试用户123",
        "password": "test123!",
        "email": "test8@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例7: 密码不包含小写字母
    print("\n测试7: 密码不包含小写字母")
    test_data = {
        "userName": "中文测试用户123",
        "password": "TEST123!",
        "email": "test9@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例8: 密码不包含数字
    print("\n测试8: 密码不包含数字")
    test_data = {
        "userName": "中文测试用户123",
        "password": "TestTest!",
        "email": "test10@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例9: 密码不包含特殊字符
    print("\n测试9: 密码不包含特殊字符")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123",
        "email": "test11@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例10: 密码与用户名相同
    print("\n测试10: 密码与用户名相同")
    test_data = {
        "userName": "中文测试用户123",
        "password": "中文测试用户123",
        "email": "test12@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例11: 邮箱格式不正确
    print("\n测试11: 邮箱格式不正确")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!",
        "email": "test13example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例12: 邮箱长度超过20个字符
    print("\n测试12: 邮箱长度超过20个字符")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!",
        "email": "test14@example.com1234567"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

def test_login_boundary_cases():
    print("\n\n测试用户登录边界情况...")
    
    # 测试用例1: 用户ID不存在
    print("\n测试1: 用户ID不存在")
    test_data = {
        "userId": "不存在的用户",
        "password": "Test123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例2: 密码错误
    print("\n测试2: 密码错误")
    test_data = {
        "userId": "中文测试用户789",
        "password": "Wrong123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例3: 用户ID格式不正确
    print("\n测试3: 用户ID格式不正确")
    test_data = {
        "userId": "中文测试@用户",
        "password": "Test123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例4: 用户ID长度小于6个字符
    print("\n测试4: 用户ID长度小于6个字符")
    test_data = {
        "userId": "中文12",
        "password": "Test123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用例5: 用户ID长度超过20个字符
    print("\n测试5: 用户ID长度超过20个字符")
    test_data = {
        "userId": "中文测试用户1234567890123",
        "password": "Test123!"
    }
    response = requests.post("http://localhost:8080/users/login", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_register_boundary_cases()
    test_login_boundary_cases()
