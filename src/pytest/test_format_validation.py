import requests
import json

# 测试格式验证
def test_format_validation():
    print("测试格式验证...")
    
    # 测试邮箱格式不正确
    print("\n测试邮箱格式不正确:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123!",
        "email": "testexample.com"  # 缺少@符号
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含大写字母
    print("\n测试密码不包含大写字母:")
    test_data = {
        "userName": "中文测试用户124",  # 新用户名
        "password": "test123!",  # 全小写
        "email": "f1@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含小写字母
    print("\n测试密码不包含小写字母:")
    test_data = {
        "userName": "中文测试用户125",  # 新用户名
        "password": "TEST123!",  # 全大写
        "email": "f2@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含数字
    print("\n测试密码不包含数字:")
    test_data = {
        "userName": "中文测试用户126",  # 新用户名
        "password": "TestTest!",  # 无数字
        "email": "f3@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码不包含特殊字符
    print("\n测试密码不包含特殊字符:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "Test123",  # 无特殊字符
        "email": "f4@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试密码与用户名相同
    print("\n测试密码与用户名相同:")
    test_data = {
        "userName": "中文测试用户123",
        "password": "中文测试用户123",  # 与用户名相同
        "email": "f5@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_format_validation()
