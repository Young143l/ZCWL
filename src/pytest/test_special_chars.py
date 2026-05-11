import requests
import json

# 测试特殊字符
def test_special_chars():
    print("测试特殊字符...")
    
    # 测试用户名包含非法特殊字符
    print("\n测试用户名包含非法特殊字符:")
    test_data = {
        "userName": "中文测试@用户",  # 包含@符号
        "password": "Test123!",
        "email": "s1@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用户名包含空格
    print("\n测试用户名包含空格:")
    test_data = {
        "userName": "中文测试 用户",  # 包含空格
        "password": "Test123!",
        "email": "s2@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")
    
    # 测试用户名包含表情符号
    print("\n测试用户名包含表情符号:")
    test_data = {
        "userName": "中文测试😀用户",  # 包含表情符号
        "password": "Test123!",
        "email": "s3@example.com"
    }
    response = requests.post("http://localhost:8080/register", headers={"Content-Type": "application/json"}, data=json.dumps(test_data))
    print(f"状态码: {response.status_code}")
    print(f"响应: {response.text}")

if __name__ == "__main__":
    test_special_chars()
