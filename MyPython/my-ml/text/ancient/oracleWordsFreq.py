#!/usr/bin/env python
# -*- coding: utf-8 -*-

from string import punctuation
from zhon.hanzi import punctuation
from langconv import *

import re
import xlwt
import docx
import sys
import os


reload(sys)
sys.setdefaultencoding('utf-8')

global wordCountDict, indexFileName
wordCountDict = {}  # 建立用于计算词频的空字典
indexFileName = {}  # 建立词频所属的文件的索引，和wordCountDict拥有相同的 key
extendDocx = ['.docx', 'doc']

# 对文本的每一行计算词频的函数
def processLine(line, fileName):
    print(line)
    # 用空格替换标点符号
    line = replaceZhonPunctuations(line)

    for word in line:
        if word in wordCountDict:
            wordCountDict[word] += 1
            if indexFileName[word].find(fileName) == -1:
                indexFileName[word] = indexFileName[word] + ',  ' + fileName
        else:
            wordCountDict[word] = 1
            indexFileName[word] = fileName
    print ('wordCountDict 賦值完畢')

def replaceZhonPunctuations(line):
    print('--------' + line)
    # 去掉其中的中文标点符号
    noZhPuncLine = re.sub(ur"[%s]+" % punctuation, "", line.decode("utf-8")) #
    # 去掉其中的英文标点符号
    noEnPuncLine = re.sub("[\s+\.\!\/_,$%^*(+\"\']+|[+——！，。？?、~@#￥%……&*（）]+".decode("utf8"), "".decode("utf8"),noZhPuncLine)
    # 去掉其中的英文或数字
    noEnLine = re.sub("[A-Za-z0-9]", "", noEnPuncLine)

    # 簡體轉化至繁體
    finalLine = Converter('zh-hans').convert(noEnLine)

    return finalLine

# 处理单个文件
def singleFileCounter(filePath, filename, isDocFile):
    try:
        if isDocFile:
            infile = docx.Document(filePath)
            for para in infile.paragraphs:
                print(para.text)
                for line in para.text:
                    print(line)
                    processLine(line, filename)
        else:
            infile = open(filePath, 'r')
            # 建立用于计算词频的空字典
            for line in infile:
                processLine(line, filename)
            infile.close()
    except:
        """
        避免出现 docx 中 PackageNotFoundError的错误，原因是一些 word 会生成隐藏的临时文件导致读取的时候失败
        """
        return



def recordDataIntoXls(sheet, items, count):
    words = []
    data = []

    sheet.col(2).width = 256 * 15

    for i in range(len(items) - 1, len(items) - count - 1, -1):
        sheet.write(i, 0, str(items[i][0]))  # 其中的'0-行, 0-列'指定表中的单元，'EnglishName'是向该单元写入的内容
        sheet.write(i, 1, str(items[i][1]))
        indexName = str(indexFileName[(items[i][1])])
        sheet.write(i, 2, indexName)

        data.append(items[i][0])
        words.append(items[i][1])


def excuteCounter(documentPath, excelName):
    wordCountDict.clear()
    # 遍历文件夹中所有的文档
    print('清空wordCountDict ')
    for fpathe, dirs, fs in os.walk(documentPath):
        if len(fs) == 0:
            print('文件夹下不包含文档')
            return False

        for filename in fs:
            print ('filename =' + filename)
            extendName = os.path.splitext(filename)[1]
            fileNameWithoutExp = os.path.splitext(filename)[0]
            filepath = os.path.join(fpathe, filename)

            print('当前文件名 ' + fileNameWithoutExp)
            if extendName in extendDocx:
                singleFileCounter(filepath, fileNameWithoutExp, True)
            elif extendName == '.txt':
                singleFileCounter(filepath, fileNameWithoutExp, False)
            else:
                continue


    # 从字典中获取数据对
    pairs = list(wordCountDict.items())
    # 列表中的数据对交换位置,数据对排序
    items = [[x, y] for (y, x) in pairs]
    items.sort(reverse=True)

    count = len(wordCountDict)

    filepath = documentPath + '/' + excelName + '.xls'  # './test1.xls'
    if os.name == 'nt': # 表示 Windows
        filepath = filepath.decode('utf-8').encode('GB2312')

    # 判断xls 是否存在，不存在就创建，存在就去覆盖写入
    if os.path.exists(filepath):
        os.remove(filepath)

    book = xlwt.Workbook(encoding='utf-8', style_compression=0)
    sheet = book.add_sheet('统计', cell_overwrite_ok=True)
    recordDataIntoXls(sheet, items, count)
    book.save(filepath)
    print (filepath)

    if os.name == 'nt':  # 表示 Windows
        os.system(filepath)
    elif os.name == 'posix':  # mac 或 linux
        os.system('open ' + filepath)
        os.system('open ' + documentPath)

    return True
