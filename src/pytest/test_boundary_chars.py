import requests
import json

# 测试边界字符
def test_boundary_chars():
    print("测试边界字符...")
    
    # 测试用户名长度边界
    print("\n测试用户名长度边界:")
    
    # 用户名长度小于6个字符
    print("1. 用户名长度小于6个字符:")
    test_data = {
        "userName": "中文12",  # 4个字符
        "password": "Test123!",
        "email": "test1@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 用户名长度超过20个字符
    print("\n2. 用户名长度超过20个字符:")
    test_data = {
        "userName": "中文测试用户12345678901234",  # 21个字符
        "password": "Test123!",
        "email": "t@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码长度边界
    print("\n测试密码长度边界:")
    
    # 密码长度小于8个字符
    print("3. 密码长度小于8个字符:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test1!",  # 7个字符
        "email": "test3@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 密码长度超过20个字符
    print("\n4. 密码长度超过20个字符:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!Test123!Test123!",  # 21个字符
        "email": "test4@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试邮箱长度边界
    print("\n测试邮箱长度边界:")
    
    # 邮箱长度超过20个字符
    print("5. 邮箱长度超过20个字符:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!",
        "email": "test5@example.com123456789"  # 21个字符
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_boundary_chars()
