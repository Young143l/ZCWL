import PyPDF2

# 打开PDF文件
with open('API接口.pdf', 'rb') as file:
    # 创建PDF阅读器对象
    reader = PyPDF2.PdfReader(file)
    
    # 获取PDF页数
    num_pages = len(reader.pages)
    print(f"PDF总页数: {num_pages}")
    
    # 读取每一页的内容
    for i in range(num_pages):
        page = reader.pages[i]
        text = page.extract_text()
        print(f"\n第{i+1}页内容:")
        print(text)
