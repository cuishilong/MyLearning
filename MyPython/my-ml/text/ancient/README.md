# Python 实现文言文词频统计
## 前言

[Github 链接](https://github.com/DeforeChen/WordsFreqCounter)

本文旨在对开发过程中出现的若干问题作记录。因为开发本身过程大多采用参考代码，不完全原始开发，因此不涉及具体的`python`语法记录，只对搭建环境及需求分析作统一的记述。

## 需求描述
### 目的

旨在开发一个词频统计工具。统计某个文件夹下所有的`.txt`,`.docx`,`.doc`文件中出现的所有单个字出现的次数。

区别于现代文和英文（现代文设计组词分词，英文则是根据空格区分单词），文言文的基本释义是一字一义。因此，我们的统计可以适用于遍历所有的汉字，然后作统计。

### 选用 python 的目的
本次的需求实际上是一个桌面端应用的开发。当前能够使用的桌面端开发工具有许多。
> * `Windows` 平台下的桌面端仍然是微软提供的 MFC 开发，采用 C++语言
> * `Mac` 平台下和`iOS` 相同，采用`Objective-C` 或 `Swift`配合`Xcode IDE`

本次的工具旨在开发一款跨平台的桌面端应用（在`Windows, MacOSX`）下通用。目前我个人已知的跨平台开发库除了`Python`相关的库外，就是`JavaScript`世界中的几个跨院库：`Electron`等。

跨平台开发本质上意味着牺牲一部分的性能以及包大小。简单来说就是各个平台本身有一套根据自身硬件性能和平台 api 特性设计的开发方案。跨平台的开发工具先用自身的开发逻辑实现了所有的业务，最后通过解释器生成原生使用的机器语言。

简单的解释，好比你带了一个翻译出国旅行。虽然也可以算得上畅行无阻，但是交流起来毕竟隔了一层。

本次的需求本身而言并不复杂，选用`python`的原因主要还是因为他的简单易用性。而`electron`，还需要熟悉更多额外的内容。

## 设计思路
![文言文统计流程图](https://upload-images.jianshu.io/upload_images/1180547-26e68865a0066fd2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 开发环境及搭建

* `python`环境 —— `python 2.7`
    * `Windows`需要额外安装，下载[这个链接](https://www.python.org/downloads/release/python-2715/)中的`Windows x86-64 MSI installer`,安装时记得勾选最后一个`add python.exe to Path`(_这一步是添加python 到环境变量，如果不勾选，回头还要手动作，很麻烦_)
    ![图片](http://upload-images.jianshu.io/upload_images/1180547-2e91629822b890fe?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
    * mac 下因为自带了`python 2.7`不需要额外安装
* `python IDE` 这没啥好说的，就是使用`JetBrains`他们家的`Pycharm`.
    这里额外说明一下，用过`pycharm`的同学可以直接跳过。
    `Pycharm`的优点在于：
    * 良好的包管理工具 —— 开发者不需要像很多教程中提到的，使用命令行进行`pip`的手动安装，直接搜索库中已有的第三方库。直接可视化操作。
    * 默认安装了虚拟环境 —— 安装虚拟环境的目的：通常地，创建一个python项目，如果有用到第三方库，我们都是直接安装在本地目录下。
    
        这样造成的一个结果就是，每当我新建一个工程，也许都存在一个不相干的共享的python库在其中。
    
        虚拟环境的搭建，旨在针对每一个新建的python工程，都有一个干净的python库在其中。下面是在`pycharm`中建立虚拟环境 `virtualEnv(virtual environment)`
         ![示意图1](https://upload-images.jianshu.io/upload_images/1180547-fc592897a0f900e8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
         ![示意图2](https://upload-images.jianshu.io/upload_images/1180547-534a424daf66deac.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 使用到的第三方库：
    * `xlwt` 生成/写入`excel`文件的库
    * `python-docx` 读取`Word`文档的第三方库
    * `zhon` 处理汉字标点符号
    * `langconv` 处理简繁体转化
    * `Tkinter` 这个是`python`自带的桌面GUI 开发工具，比较轻量级。最后的效果大概如下
    ![mac 端的视图](https://upload-images.jianshu.io/upload_images/1180547-ab6839c54e94d1f3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


## 遇到的问题

### 业务逻辑上的问题
除了上面提到的程序上的问题外，业务本身还存在几个开发过程中需要解决的痛点。这里一一罗列：

* 标点符号 —— 我们统计词频的时候，需要做的事就是“掐头去尾”，即，遍历每一行的字符串内容，去掉其中的所有标点符号（包括中英文），然后作下一步操作
* 简繁转化 —— 简体字和繁体字并不是一一对应的关系。比如繁体字的"麵條"和“面對”，对应简体字都是“面”，繁转简还好说，简转繁的时候，究竟对应哪个面，也许就不得而知了。
    目前的做法是统一转化成简体。但是一些特殊情况也会出问题，比如“乾隆”和“乾坤”，转成简体字，会变成“干隆”和“干坤”。

    之前使用的简繁转换库叫做`opencc`，之后发现打包后运行时，会报转化失败的错误。故而改用一个引入源码的库`langconv.py`
* 读取 `.txt`和`.docx`/`.doc`的区别
    `Word`相对于记事本，是支持富文本的。因此在文件的读取上也有很多的不同。这里专门使用了一个`python`的`Word`读取库叫做`python-docx`作读取操作。

### 开发中遇到的问题

* 不同平台下的若干问题
    * 打包问题 —— 使用什么打包工具
        网上有很多的文章，提到了windows 平台使用`py2exe`等等的库，mac 平台使用`py2app`.实际情况是，这些库都不尽如人意，存在许许多多的问题。不是打包失败，就是打包成功后运行失败，且无法定位问题。

        闲话少叙，这里使用的是叫做`Pyinstaller`的打包工具。
        
        这里也有一个小坑：首次安装`pyinstaller`后，在虚拟环境中执行终端的打包指令是会报无法识别`pyinstaller`的。这时候必须要将整个`pycharm`重启。当看到终端指令前面出现(venv)的字样，才能执行。
        ![示意图3](https://upload-images.jianshu.io/upload_images/1180547-2115a23b0332aa9f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

        
        打包mac 下的 app，那么要在 mac 下运行。打包 Windows 的.exe 文件，那么工程必须运行在 Windows 下。也就是说，我在 A 平台的时候，不能打包 B 平台下的包。这个其实也蛮麻烦的，等于我在两个平台都要安装一遍程序。
        
    * 打开文件/文件夹，mac 和 windows 下的命令不同，代码要做兼容
    完成统计后，我们理想的状态是打开生成的`excel`和它所在的文件夹。
        * mac 底下用的是`open ./desExcel.xls`,
        * windows 底下的命令是`./desExcel.xls`
    
        针对这点，需要作区别处理完成兼容
* 调试过程中可以正常使用，打包后无法运行。—— 涉及打包后如何定位问题
    在`mac`平台，打包后在`dist`文件夹下会生成两个文件，一个是`.app`，一个是一个终端文件，直接拖曳进入终端后可以执行，且会出现打印日志，方便定位运行失败问题。
    ![示意图4](https://upload-images.jianshu.io/upload_images/1180547-6b3f9b8b41f1447e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

* 异常处理 —— 对于`Word`会生成隐藏的临时文件，当临时文件为空时，`python`中的读取会报错。
    这里可以使用`try` `except`语法忽略空文件。因为涉及语法，不赘述。

以上。

