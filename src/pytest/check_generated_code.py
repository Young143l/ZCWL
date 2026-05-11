#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
检查生成的前端代码内容
"""

import requests
import json
import sys
import io
import os

# 设置环境变量确保终端使用UTF-8
os.environ['PYTHONIOENCODING'] = 'utf-8'
# 设置标准输出编码为UTF-8
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 测试配置
BASE_URL = "http://localhost:8080"
LOGIN_URL = f"{BASE_URL}/users/login"
CODE_SF_URL = f"{BASE_URL}/code/sf"

# 测试用户信息
TEST_USER = {
    "userId": "testuser",
    "password": "Test12345678!"
}


def test_login():
    """测试登录获取token"""
    response = requests.post(LOGIN_URL, json=TEST_USER)
    if response.status_code == 200:
        data = response.json()
        return data.get("token")
    else:
        print(f"登录失败: {response.text}")
        return None


def check_project_code(token, sf_id):
    """检查项目代码内容"""
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.get(f"{CODE_SF_URL}/{sf_id}", headers=headers)
    if response.status_code == 200:
        data = response.json()
        print(f"项目名称: {data.get('name')}")
        print(f"项目ID: {data.get('sfId')}")
        code = data.get('code', {})
        print("\nHTML内容:")
        print(code.get('html', ''))
        print("\nCSS内容:")
        print(code.get('css', ''))
        print("\nJavaScript内容:")
        print(code.get('javascript', ''))
    else:
        print(f"获取项目失败: {response.text}")


def main():
    """主函数"""
    # 登录获取token
    token = test_login()
    if not token:
        print("登录失败，无法继续测试")
        return
    
    # 检查项目代码 - 使用测试中生成的项目ID
    sf_id = "sf_9670f8bb4f8d"
    check_project_code(token, sf_id)


if __name__ == "__main__":
    main()
