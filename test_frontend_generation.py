#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
测试单页前端生成功能
"""

import requests
import json
import time

# 测试配置
BASE_URL = "http://localhost:8080"
LOGIN_URL = f"{BASE_URL}/users/login"
CODE_SF_URL = f"{BASE_URL}/code/sf"

# 测试用户信息
TEST_USER = {
    "userId": "testuser",
    "password": "Test12345678!"
}

# 测试项目信息
TEST_PROJECT = {
    "uId": "testuser",
    "projectName": f"测试项目_{int(time.time())}",
    "message": "创建一个简单的待办事项应用，包含添加、删除、标记完成功能，使用现代CSS样式"
}

# 测试更新项目信息
UPDATE_MESSAGE = "添加深色模式切换功能"


def test_login():
    """测试登录获取token"""
    print("=== 测试登录 ===")
    response = requests.post(LOGIN_URL, json=TEST_USER)
    print(f"登录状态码: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("登录成功")
        return data.get("token")
    else:
        print(f"登录失败: {response.text}")
        return None


def test_create_frontend_project(token):
    """测试创建前端项目"""
    print("\n=== 测试创建前端项目 ===")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.post(CODE_SF_URL, json=TEST_PROJECT, headers=headers)
    print(f"创建项目状态码: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("创建项目成功")
        print(f"项目ID: {data.get('sfId')}")
        # 检查返回的代码
        code = data.get('code', {})
        print(f"生成的HTML长度: {len(code.get('html', ''))}")
        print(f"生成的CSS长度: {len(code.get('css', ''))}")
        print(f"生成的JavaScript长度: {len(code.get('javascript', ''))}")
        return data.get('sfId'), code
    else:
        print(f"创建项目失败: {response.text}")
        return None, None


def test_get_frontend_project(token, sf_id):
    """测试获取前端项目信息"""
    print("\n=== 测试获取前端项目信息 ===")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.get(f"{CODE_SF_URL}/{sf_id}", headers=headers)
    print(f"获取项目状态码: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("获取项目成功")
        print(f"项目名称: {data.get('name')}")
        print(f"项目ID: {data.get('sfId')}")
        # 检查返回的代码
        code = data.get('code', {})
        print(f"HTML长度: {len(code.get('html', ''))}")
        print(f"CSS长度: {len(code.get('css', ''))}")
        print(f"JavaScript长度: {len(code.get('javascript', ''))}")
        return code
    else:
        print(f"获取项目失败: {response.text}")
        return None


def test_update_frontend_project(token, sf_id, current_code):
    """测试更新前端项目"""
    print("\n=== 测试更新前端项目 ===")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    update_data = {
        "code": current_code,
        "message": UPDATE_MESSAGE,
        "selectId": []
    }
    response = requests.post(f"{CODE_SF_URL}/{sf_id}", json=update_data, headers=headers)
    print(f"更新项目状态码: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("更新项目成功")
        # 检查返回的代码
        code = data.get('code', {})
        print(f"更新后的HTML长度: {len(code.get('html', ''))}")
        print(f"更新后的CSS长度: {len(code.get('css', ''))}")
        print(f"更新后的JavaScript长度: {len(code.get('javascript', ''))}")
        return code
    else:
        print(f"更新项目失败: {response.text}")
        return None


def test_get_project_list(token):
    """测试获取项目列表"""
    print("\n=== 测试获取项目列表 ===")
    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }
    response = requests.get(f"{CODE_SF_URL}?uId=testuser", headers=headers)
    print(f"获取项目列表状态码: {response.status_code}")
    if response.status_code == 200:
        data = response.json()
        print("获取项目列表成功")
        projects = data.get('projects', [])
        print(f"项目数量: {len(projects)}")
        for project in projects:
            print(f"- {project.get('projectName')} (ID: {project.get('sfId')})")
        return projects
    else:
        print(f"获取项目列表失败: {response.text}")
        return []


def main():
    """主测试函数"""
    print("开始测试单页前端生成功能")
    
    # 1. 登录获取token
    token = test_login()
    if not token:
        print("登录失败，无法继续测试")
        return
    
    # 2. 创建前端项目
    sf_id, created_code = test_create_frontend_project(token)
    if not sf_id:
        print("创建项目失败，无法继续测试")
        return
    
    # 3. 获取项目信息
    retrieved_code = test_get_frontend_project(token, sf_id)
    
    # 4. 更新项目
    updated_code = test_update_frontend_project(token, sf_id, retrieved_code)
    
    # 5. 获取项目列表
    test_get_project_list(token)
    
    print("\n测试完成")


if __name__ == "__main__":
    main()
