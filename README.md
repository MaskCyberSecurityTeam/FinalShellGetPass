# 介绍

FinalShellGetPass是一款FinalShell密码读取工具

# 使用

Win: `FinalShellGetPass.exe X:\FinalShell的安装目录\conn\`

Mac: `FinalShellGetPass ~/Library/FinalShell/conn/`

Linux: `暂时没环境编译，有空再弄！`

![image](https://user-images.githubusercontent.com/30547741/204143824-96112e3e-8150-40be-b8e1-691bae62fdfd.png)

# 大小说明

编译方式使用的是Graalvm但是因为需要内置jvm.dll所以体积还是会大一些，Windows上可以用upx正常压缩到4M大小，MacOS上upx压缩不成功所以比较大(14M)。

# 参考说明

本项目只是将它稍做修改后编译成了可执行文件，方便使用。核心解密代码来自: https://github.com/jas502n/FinalShellDecodePass

# 编译环境

Windows上需要使用Graalvm-21进行编译，不然upx压缩了运行不了。
