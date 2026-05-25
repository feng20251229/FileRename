package Replace;
import java.util.Scanner;
import java.io.*;
public class replace {
    public static void main(String[] args) {
        System.out.println("请输入所需要批量修改的文件夹地址");
        Scanner sc = new Scanner(System.in);
        String fileAddress = sc.nextLine();
        File renameFile = new File(fileAddress);
        //判断输入是否正确
        if (!renameFile.exists()) {
            System.out.println("文件夹不存在");
            return ;
        }
        if (!renameFile.isDirectory()) {
            System.out.println("该地址不是文件夹");
            return ;
        }
        File[] file = renameFile.listFiles();
        if (file.length == 0) {
            System.out.println("该文件夹没有文件");
            return ;
        }
        //更改文档名字
        for (int i = 0; i < file.length; i++) {
            File oldFile = file[i];
            String pathFile = oldFile.getParent();
            String oldName = oldFile.getName();
            String newName = "new_" + oldName;

            File newFile = new File(pathFile, newName);
            oldFile.renameTo(newFile);
            System.out.println( "旧文档名 " + oldName + " 更改为 " + newName);
        }

        System.out.println("完成所有命名更改");
        sc.close();
    }
}
