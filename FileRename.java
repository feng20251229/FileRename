package replace;
import java.util.Scanner;
import java.io.*;

public class FileRename {
    public static void main(String[] args) {
        FileRename renameToFile = new FileRename();
        try {
            renameToFile.run();
        } catch(UnRightInput e) {
            e.showMessage();
        }
    }

    private void run() throws UnRightInput {
        try(Scanner sc = new Scanner(System.in)) {
            System.out.println("请输入需要批量更改文件的文件夹路径");
            File oldFile = new File(sc.nextLine());
            System.out.println("需要添加的前缀");
            String add = sc.nextLine();

            File[] files = validGetFiles(oldFile);
            if(files == null || !isRightPre(add)) {
                throw new UnRightInput();
            }
            prePrint(files, add);

            System.out.println("确认是否修改(y/n)");
            String input = sc.nextLine();
            if(!input.equals("y")
            && !input.equals("Y")
            && !input.equals("yes")) {
                System.out.println("已为您结束修改");
                return ;
            }

            renameFile(files, add);
        }
        System.out.println("完成");
    }

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

    private boolean isRightPre(String add) {
        boolean is = add.matches("[a-zA-Z]\\w*");
        if (!is) {
            System.out.println("前缀不符合命名规则");
        }
        return is;
    }

    private void prePrint(File[] files, String add) {
        for (File file : files) {
            System.out.println(file.getName() + "->" + add + file.getName());
        }
    }


    private void renameFile(File[] files, String add) {
        int successCount = 0;
        int failCount = 0;

        for (File oldFile : files) {
                String newName = add + oldFile.getName();
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

class UnRightInput extends Exception {
    public UnRightInput() {}

    public void showMessage() {
        System.out.println("错误XXX");
    }
}

