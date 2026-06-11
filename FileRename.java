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
            System.out.println("请输入需要批量更改文件的文件夹路径");
            File oldFile = new File(sc.nextLine());
            File[] files = validGetFiles(oldFile);

            System.out.println("请选择功能");
            System.out.println("1. 添加前缀");
            System.out.println("2. 删除前缀");
            System.out.println("3. 替换文本");
            System.out.println("4. 数字编号格式化(2-1 -> 2-01)");

            System.out.println("输入数字选择");
            int number = sc.nextInt();
            sc.nextLine();

            RenameStrategy strategy = null;
            if( number == 1 || number == 2){
                System.out.println("需要添加的前缀");
                String add = sc.nextLine();
                if (number == 1) {
                    strategy = new AddPrefix(files, add);
                }
                else {
                    strategy = new RemovePrefix();
                }

                if(files == null || !isRightPre(add)) {
                    throw new UnRightInput();
                }
            } else if (number == 3) {
                strategy = new Replace();
            } else {
                strategy = new ReplaceDigit();
            }
            
            strategy.prePrint();

            System.out.println("确认是否修改(y/n)");
            String input = sc.nextLine();
            if(!input.equals("y")
            && !input.equals("Y")
            && !input.equals("yes")) {
                System.out.println("已为您结束修改");
                return ;
            }

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
        for (File file : files) {
            System.out.println(file.getName() + "->" + prefix + file.getName());
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
                    System.out.println( "旧文档名 " + oldFile.getName());
                    successCount++;
                }
        }

        System.out.println("改名完成：成功：" + successCount + "，失败 " + failCount);
    }
}

class RemovePrefix implements RenameStrategy {
    public void prePrint() {

    }
    public void renameFile() {

    }
}

class Replace implements RenameStrategy {
    public void prePrint() {

    }
    public void renameFile() {

    }
}

class ReplaceDigit implements RenameStrategy {
    public void prePrint() {

    }
    public void renameFile() {

    }
}