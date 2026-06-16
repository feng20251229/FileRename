# FileRename —— 批量文件重命名工具

一个支持四种改名策略、配置记忆、GUI选文件夹、操作日志的Java批量文件重命名工具。

## 功能

- **四种改名策略**
  - 添加前缀
  - 删除前缀
  - 替换文本
  - 编号格式化（如 2-1 → 2-01）
- **配置记忆**：记住上次使用的文件夹、功能、前缀
- **GUI选文件夹**：图形化选择目录，不用手输路径
- **操作日志**：记录每次改名操作
- **循环处理**：一次运行可处理多个文件夹

## 技术栈

- Java
- Swing (JFileChooser)
- Java Logging

## 使用方法

```bash
# 编译
javac -d . FileRename.java

# 运行
java replace.FileRename
