package Replace;
import java.util.Scanner;
import java.io.*;
public class replace {
    public static void main(String[] args) {
        replace re = new replace();
        re.run();
    }

    private void run(){
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("请输入所需要批量修改的文件夹地址");
            File renameFile = new File(sc.nextLine());

            //判断输入是否正确
            isUsefulDirectory(renameFile);
            //预览
            this.previewRenames(renameFile, "new_");
            //更改文档名字
            
        }
        System.out.println("完成所有命名更改");
    }
    
    private void previewRenames(File files, String prefix) {
        System.out.println("预览改名");
        for (File f : files.listFiles()) {
            System.out.println(f.getName() + " -> " + prefix + f.getName());
        }
    }

    private boolean isUsefulDirectory(File file) {
        
    }

    private void wayToRename(int n, File file) {
        System.out.println("请选择修改方式， 输入对应数字");
        System.out.println("1 增加前缀")
        if (n == 1) {
            this.way1(file.listFiles());
        }
        else {
            System.out.println("其他功能尚未解锁");
        }
    }

    private void way1(File[] file) {
        
    }
}
