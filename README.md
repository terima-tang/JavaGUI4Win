# JavaGUI4Win
这是一个集成jdk或jre以及mysql的Java GUI桌面程序安装包，使用inno setup进行打包，可以在没有安装jdk或jre以及没有安装mysql的pc上安装使用Java桌面程序
- 第一步：编译java源代码，生成jar文件
- 第二步：安装inno setup，按照4.iss文件中设置的文件路径创建好文件夹
-  <img width="461" height="144" alt="image" src="https://github.com/user-attachments/assets/00733094-cda5-46bb-ab2e-233a015a0635" />

- 下载jdk17并放置于文件夹相应位置
- 运行4.iss进行打包，结束后生成.exe文件，可在没有安装jdk以及mysql的windows操作系统中直接一步安装使用，无需单独安装jdk以及mysql
- 注意:如果安装在C:盘，记得使用管理员权限，打开桌面图标
