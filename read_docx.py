import zipfile
import xml.etree.ElementTree as ET
import os
import sys

# 设置输出编码
sys.stdout.reconfigure(encoding='utf-8')

path = r'D:\desktop\毕设\开题\计科2202_2022013268柳鸿博开题报告.docx'

z = zipfile.ZipFile(path)
content = z.read('word/document.xml').decode('utf-8')
root = ET.fromstring(content)
ns = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}

# 按段落提取文本
paragraphs = root.findall('.//w:p', ns)
for para in paragraphs:
    texts = [t.text for t in para.findall('.//w:t', ns) if t.text]
    line = ''.join(texts).replace('\xa0', ' ').strip()
    if line:
        print(line)
