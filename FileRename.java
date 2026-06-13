package replace;
import java.util.Scanner;
import java.io.*;

interface RenameStrategy {
    void prePrint();
    void renameFile();
}

public class FileRename {
    //运行主程序
    public static void main(String[] args) {
        FileRename renameToFile = new FileRename();
        try {
            renameToFile.run();
        } catch(UnRightInput e) {
            e.showMessage();
        }
    }
    //实际运行程序
    private void run() throws UnRightInput {
        try(Scanner sc = new Scanner(System.in)) {
            //输入需要更改文件夹的路径
            System.out.println("请输入需要批量更改文件的文件夹路径");
            File oldFile = new File(sc.nextLine());
            File[] files = validGetFiles(oldFile);
            //选择功能
            System.out.println("请选择功能");
            System.out.println("1. 添加前缀");
            System.out.println("2. 删除前缀");
            System.out.println("3. 替换文本");
            System.out.println("4. 数字编号格式化(2-1 -> 2-01)");

            System.out.println("输入数字选择");
            int number = sc.nextInt();
            sc.nextLine();
            //应用面向接口
            RenameStrategy strategy = null;
            switch (number) {
                case 1:
                    System.out.println("需要添加的前缀");
                    String pre = sc.nextLine();
                    if(files == null || !isRightPre(pre)) {
                        throw new UnRightInput();
                    }
                    strategy = new AddPrefix(files, pre);
                    break;
                case 2:
                    System.out.println("需要删除的前缀");
                    strategy = new RemovePrefix(files, sc.nextLine());
                    break;
                case 3:
                    System.out.println("需要替换的原文本：");
                    String target = sc.nextLine();
                    System.out.println("需要替换成的新文本：");
                    String replacement = sc.nextLine();
                    strategy = new Replace(files, target, replacement);
                    break;
                case 4:
                    strategy = new ReplaceDigit(files);
                    break;
                default:
                    System.out.println("没有");
                    return;
            }
            //预览打印
            strategy.prePrint();

            System.out.println("确认是否修改(y/n)");
            String input = sc.nextLine();
            if(!input.equals("y")
            && !input.equals("Y")
            && !input.equals("yes")) {
                System.out.println("已为您结束修改");
                return ;
            }
            //完成修改
            strategy.renameFile();
        }
        System.out.println("完成");
    }
    //判断是否是有效的文件夹
    private File[] validGetFiles(File oldFile) {
        boolean is = true;
        if (!oldFile.exists()) {
            System.out.println("文件夹不存在");
            is = false;
        }
        if (!oldFile.isDirectory()) {
            System.out.println("该地址不是文件夹");
            is = false;
        }
        File[] files = oldFile.listFiles();
            if (files == null || files.length == 0) {
                System.out.println("该文件夹没有文件");
                is = false;
            }
        return is ? files : null;
    }
    //判断添加前缀是否合法
    private boolean isRightPre(String add) {
        boolean is = add.matches("[a-zA-Z]\\w*");
        if (!is) {
            System.out.println("前缀不符合命名规则");
        }
        return is;
    }
}

class UnRightInput extends Exception {
    public UnRightInput() {}

    public void showMessage() {
        System.out.println("错误XXX");
    }
}

class AddPrefix implements RenameStrategy {
    private File[] files;
    private String prefix;

    AddPrefix(File[] files, String pre) {
        this.files = files;
        prefix = pre;
    }

    public void prePrint() {
        for (File oldFile : files) {
            System.out.println(oldFile.getName() + "->" + prefix + oldFile.getName());
        }
    }

    public void renameFile() {
        int successCount = 0;
        int failCount = 0;

        for (File oldFile : files) {
                String newName = prefix + oldFile.getName();
                File newFile = new File(oldFile.getParent(), newName);

                if (newFile.exists()) {
                    System.out.println("跳过（已存在）: " + newName);
                    continue;
                }

                if (!oldFile.renameTo(newFile)) {
                    System.out.println("改名失败: " + oldFile.getAbsolutePath());
                    failCount++;
                } else {
                    System.out.println("成功: " + oldFile.getName() + " -> " + newName);
                    successCount++;
                }
        }

        System.out.println("改名完成：成功：" + successCount + "，失败：" + failCount);
    }
}

class RemovePrefix implements RenameStrategy {
    private File[] files;
    private String preMove;

    RemovePrefix(File[] files, String pre) {
        this.files = files;
        preMove = pre;
    }

    public void prePrint() {
        for (File oldFile : files) {
            String oldName = oldFile.getName();
            if (oldName.startsWith(preMove)) {
                System.out.println(oldName + "->" + oldName.substring(preMove.length()));
            } else {
                System.out.println(oldName + "没有修改");
            }
        }
    }
    public void renameFile() {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (File oldFile : files) {
            String oldName = oldFile.getName();
            if (oldName.startsWith(preMove)) {
                String newName = oldName.substring(preMove.length());
                File newFile = new File(oldFile.getParent(), newName);

                if (newFile.exists()) {
                    System.out.println("跳过（存在）" + newName);
                    skipCount++;
                    continue;
                }

                if (!oldFile.renameTo(newFile)) {
                    System.out.println("改名失败" + oldFile.getAbsolutePath());
                    failCount++;
                } else {
                    System.out.println("旧文档名" + oldName);
                    successCount++;
                }
            } else {
                System.out.println("跳过（无前缀）" + oldFile.getAbsolutePath());
            }
        }

        System.out.println("改名完成：成功：" + successCount + "，失败：" + failCount + "，跳过：" + skipCount);
    }
}

class Replace implements RenameStrategy {
    private File[] files;
    private String target;      // 要替换的旧文本
    private String replacement;

    Replace(File[] files, String target, String replacement) {
        this.files = files;
        this.target = target;
        this.replacement = replacement;
    }

    public void prePrint() {
        for (File oldFile : files) {
            String oldName = oldFile.getName();
            String newName = oldName.replace(target, replacement);
            System.out.println(oldName + " -> " + newName);
        }
    }

    public void renameFile() {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (File oldFile : files) {
            String oldName = oldFile.getName();
            String newName = oldName.replace(target, replacement);

            // 如果名字没变，跳过
            if (oldName.equals(newName)) {
                System.out.println("跳过（无变化）: " + oldName);
                skipCount++;
                continue;
            }

            File newFile = new File(oldFile.getParent(), newName);

            // 如果目标文件已存在，跳过
            if (newFile.exists()) {
                System.out.println("跳过（已存在）: " + newName);
                skipCount++;
                continue;
            }

            // 执行重命名
            if (oldFile.renameTo(newFile)) {
                System.out.println("成功: " + oldName + " -> " + newName);
                successCount++;
            } else {
                System.out.println("失败: " + oldName);
                failCount++;
            }
        }

        System.out.println("替换完成：成功 " + successCount + "，失败 " + failCount + "，跳过 " + skipCount);
    }
}

class ReplaceDigit implements RenameStrategy {
    private File[] files;

    ReplaceDigit(File[] files) {
        this.files = files;
    }

    //把个位数格式化成两位数
    private String formatNumber(String oldName) {
        // 匹配模式：- 后面跟一个数字，然后是 .扩展名
        // 例如：2-1.mp4 中的 "-1.mp4"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-(\\d)(\\.[^.]+$)");
        java.util.regex.Matcher matcher = pattern.matcher(oldName);
        
        if (matcher.find()) {
            String digit = matcher.group(1);      // 个位数 "1"
            String extension = matcher.group(2);  // ".mp4"
            // 替换成 -0数字 + 扩展名
            return oldName.replaceFirst("-" + digit + "\\" + extension, "-0" + digit + extension);
        }
        return oldName;  // 不匹配则不变
    }

    public void prePrint() {
        for (File oldFile : files) {
            String oldName = oldFile.getName();
            String newName = formatNumber(oldName);
            System.out.println(oldName + " -> " + newName);
        }
    }

    public void renameFile() {
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;

        for (File oldFile : files) {
            String oldName = oldFile.getName();
            String newName = formatNumber(oldName);

            // 如果名字没变，跳过
            if (oldName.equals(newName)) {
                System.out.println("跳过（已经是两位数格式或无匹配）: " + oldName);
                skipCount++;
                continue;
            }

            File newFile = new File(oldFile.getParent(), newName);

            // 如果目标文件已存在，跳过
            if (newFile.exists()) {
                System.out.println("跳过（已存在）: " + newName);
                skipCount++;
                continue;
            }

            // 执行重命名
            if (oldFile.renameTo(newFile)) {
                System.out.println("成功: " + oldName + " -> " + newName);
                successCount++;
            } else {
                System.out.println("失败: " + oldName);
                failCount++;
            }
        }

        System.out.println("格式化完成：成功 " + successCount + "，失败 " + failCount + "，跳过 " + skipCount);
    }
}