import requests
import json

# 测试修复后的验证功能
def test_fix_validation():
    print("测试修复后的验证功能...")
    
    # 测试用户名长度超过20个字符
    print("\n测试1: 用户名长度超过20个字符")
    test_data = {
        "userName": "中文测试用户123456789012345",  # 22个字符
        "password": "Test123!",
        "email": "fix11@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含大写字母
    print("\n测试2: 密码不包含大写字母")
    test_data = {
        "userName": "中文测试用户129",
        "password": "test123!",  # 全小写
        "email": "fix2@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含小写字母
    print("\n测试3: 密码不包含小写字母")
    test_data = {
        "userName": "中文测试用户130",
        "password": "TEST123!",  # 全大写
        "email": "fix3@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含数字
    print("\n测试4: 密码不包含数字")
    test_data = {
        "userName": "中文测试用户131",
        "password": "TestTest!",  # 无数字
        "email": "fix4@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含特殊字符
    print("\n测试5: 密码不包含特殊字符")
    test_data = {
        "userName": "中文测试用户132",
        "password": "Test123456",  # 无特殊字符，长度符合要求
        "email": "fix5@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_fix_validation()
