#!/usr/bin/python
# -*- coding: UTF-8 -*-

from Tkinter import *
import tkMessageBox
import tkFileDialog
from oracleWordsFreq import *

class Application(Frame):
    def __init__(self, master=None):
        Frame.__init__(self, master)

        self.filepath = '../tokenizer/'  #给定一个默认值
        self.pack(fill = BOTH)
        self.createWidgets()

    def createWidgets(self):
        # create title
        self.titleLabel = Label(self, text='郡公亲用')
        self.titleLabel.pack(side = TOP, pady=16)

        self.textInputLabel = Label(self, text = '表名')
        self.textInputLabel.pack(side = LEFT, padx = 8)

        self.excelFileNameEntry = Entry(self)
        self.excelFileNameEntry.pack(side = LEFT, fill = X, expand = 1, padx = 8)

        # 开始生成统计表
        self.startCounterButton = Button(self, text='开始统计', command=self.startCounter)
        self.startCounterButton.pack(side=RIGHT, padx = 8)

        # 选择文件夹按钮
        self.selectPathButton = Button(self, text='选择路径', command=self.selectDocPath)
        self.selectPathButton.pack(side=RIGHT, padx = 8)

    def selectDocPath(self):
        print ('选择路径')
        self.filepath = tkFileDialog.askdirectory()
        print(self.filepath)

    def startCounter(self):
        excelName = self.excelFileNameEntry.get()
        if len(excelName) == 0:
            tkMessageBox.showinfo('警告', '请输入生成的 excel 文件名')
        else:
            res = excuteCounter(self.filepath, excelName)
            if res == False:
                tkMessageBox.showinfo('警告', '当前文件夹下不包含文档')



app = Application()
# 设置窗口标题:
app.master.wm_title('敕造词频录')
app.master.geometry('500x100+500+500')
app.master['bg'] = '#ffffff'


# 主消息循环:
app.mainloop()