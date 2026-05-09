"""
测试页面启动脚本

功能: 启动一个本地HTTP服务器，用于访问test/index.html页面
使用方法:
    python test/run.py           启动测试页面服务 (默认端口 8080)
    python test/run.py --port 3000  指定端口启动
"""

import os
import sys
import http.server
import socketserver
import webbrowser
import threading


# 配置参数
DEFAULT_PORT = 8001  # 默认服务端口
HOST = "localhost"   # 服务地址


def run(port=DEFAULT_PORT):
    """
    启动HTTP服务器并打开浏览器访问index.html页面
    
    参数:
        port (int): HTTP服务器端口号，默认8080
    """
    # 获取test文件夹路径
    test_dir = os.path.dirname(os.path.abspath(__file__))
    index_file = os.path.join(test_dir, "index.html")
    
    # 检查index.html是否存在
    if not os.path.exists(index_file):
        print(f"错误: 找不到index.html文件 ({index_file})")
        return
    
    # 切换到test目录
    os.chdir(test_dir)
    
    # 创建HTTP请求处理器
    handler = http.server.SimpleHTTPRequestHandler
    
    # 创建TCP服务器
    with socketserver.TCPServer((HOST, port), handler) as httpd:
        print(f"测试页面服务已启动: http://{HOST}:{port}")
        print(f"访问地址: http://{HOST}:{port}/index.html")
        print("按 Ctrl+C 停止服务")
        
        # 在浏览器中自动打开页面
        threading.Timer(1, lambda: webbrowser.open(f"http://{HOST}:{port}/index.html")).start()
        
        # 启动服务器
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n服务已停止")
            sys.exit(0)


if __name__ == "__main__":
    # 解析命令行参数
    port = DEFAULT_PORT
    
    # 支持 --port 参数指定端口
    if "--port" in sys.argv:
        try:
            port_index = sys.argv.index("--port")
            if port_index + 1 < len(sys.argv):
                port = int(sys.argv[port_index + 1])
        except (ValueError, IndexError):
            print(f"无效的端口号，使用默认端口 {DEFAULT_PORT}")
    
    run(port=port)
