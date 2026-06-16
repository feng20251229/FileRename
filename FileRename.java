package replace;

import java.util.Scanner;
import java.util.Properties;
import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

//抽象接口用来整合修改方法
interface RenameStrategy {
    void prePrint();

    void renameFile();
}

// 配置器
class ConfigManager {
    private static final String CONFIG_FILE = "rename.config";
    private Properties props = new Properties();

    public ConfigManager() {
        load();
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
        save();
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    private void load() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "FileRename Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class FileRename {
    private ConfigManager config = new ConfigManager();

    // 运行主程序
    public static void main(String[] args) {
        // 换一个系统主题
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // JFileChooser chooser = new JFileChooser();

        FileRename renameToFile = new FileRename();
        try {
            renameToFile.run();
        } catch (UnRightInput e) {
            e.showMessage();
        }
    }

    // 实际运行程序
    private void run() throws UnRightInput {
        Scanner sc = new Scanner(System.in);
        while (true) {
            try {
                // 输入需要更改文件夹的路径
                File[] files = selectFolder();
                // 选择功能
                int choice = selectFuntion(sc);
                // 应用面向接口
                RenameStrategy strategy = createStrategy(sc, choice, files);
                // 预览打印
                strategy.prePrint();
                // 确认
                if (!userConfirmed(sc)) {
                    System.out.println("已取消");
                    continue;
                }
                // 完成修改
                strategy.renameFile();
                // 退出
                if (!userInsisted(sc)) {
                    break;
                }
            } catch (UnRightInput e) {
                System.out.println("操作取消或输入无效，请重新开始");
                e.showMessage();
            } catch (Exception e) {
                System.out.println("发生未知错误，程序将退出");
                e.printStackTrace();
                break;
            }
        }
        System.out.println("完成");
    }

    // 创建图形化菜单选择器
    private File chooseFolder() {
        // 创建文件夹选择器
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("请选择文件夹");

        String lastPath = config.get("last.folder", "");
        if (!lastPath.isEmpty()) {
            File lastFolder = new File(lastPath);
            if (lastFolder.exists() && lastFolder.isDirectory()) {
                chooser.setCurrentDirectory(lastFolder);
            } else {
                chooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
            }
        } else {
            chooser.setCurrentDirectory(FileSystemView.getFileSystemView().getHomeDirectory());
        }

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            config.set("last.folder", selected.getAbsolutePath());
            return selected;
        }
        return null;
    }

    // 选择文件夹
    private File[] selectFolder() throws UnRightInput {
        File oldFile = chooseFolder();
        if (oldFile == null) {
            throw new UnRightInput();
        }
        File[] files = validGetFiles(oldFile);
        return files;
    }

    // 选择功能
    private int selectFuntion(Scanner sc) {
        String lastChoice = config.get("last.choice", "");
        System.out.println("请选择功能(直接回车用上次：" + lastChoice + ")");
        System.out.println("1. 添加前缀");
        System.out.println("2. 删除前缀");
        System.out.println("3. 替换文本");
        System.out.println("4. 数字编号格式化(2-1 -> 2-01)");
        System.out.println("输入数字选择");

        String input = sc.nextLine();
        // 用回车实现操作的具体机制
        if (input.trim().isEmpty()) {
            if (lastChoice.isEmpty()) {
                System.out.println("没有上次记录，请重新输入");
                return selectFuntion(sc);
            }
            input = lastChoice;
        }

        config.set("last.choice", input);
        return Integer.parseInt(input);
    }

    // 对应创建修改类
    private RenameStrategy createStrategy(Scanner sc, int choice, File[] files) throws UnRightInput {
        switch (choice) {
            case 1:
                String lastPrefix = config.get("last.prefix", "");
                System.out.println("需要添加的前缀（直接回车用上次：" + lastPrefix + "）");
                String pre = sc.nextLine();
                if (pre.isEmpty() && !lastPrefix.isEmpty()) {
                    pre = lastPrefix;
                }
                if (files == null || !isRightPre(pre)) {
                    throw new UnRightInput();
                }
                config.set("last.prefix", pre);
                return new AddPrefix(files, pre);
            case 2:
                System.out.println("需要删除的前缀");
                return new RemovePrefix(files, sc.nextLine());
            case 3:
                System.out.println("需要替换的原文本：");
                String target = sc.nextLine();
                System.out.println("需要替换成的新文本：");
                String replacement = sc.nextLine();
                return new Replace(files, target, replacement);
            case 4:
                return new ReplaceDigit(files);
            default:
                System.out.println("没有");
                return null;
        }
    }

    // 手动确定
    private boolean userConfirmed(Scanner sc) {
        System.out.println("确认是否修改(y/n)");
        String input = sc.nextLine();
        return input.equals("y") && !input.equals("Y") && !input.equals("yes");
    }

    // 手动退出
    private boolean userInsisted(Scanner sc) {
        System.out.print("是否继续处理其他文件？(y/n): ");
        String input = sc.nextLine();
        return input.equals("y") && !input.equals("Y") && !input.equals("yes");
    }

    // 判断是否是有效的文件夹
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

    // 判断添加前缀是否合法
    private boolean isRightPre(String add) {
        boolean is = add.matches("[a-zA-Z]\\w*");
        if (!is) {
            System.out.println("前缀不符合命名规则");
        }
        return is;
    }
}

class UnRightInput extends Exception {
    public UnRightInput() {
    }

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
    private String target; // 要替换的旧文本
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

    // 把个位数格式化成两位数
    private String formatNumber(String oldName) {
        // 匹配模式：- 后面跟一个数字，然后是 .扩展名
        // 例如：2-1.mp4 中的 "-1.mp4"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("-(\\d)(\\.[^.]+$)");
        java.util.regex.Matcher matcher = pattern.matcher(oldName);

        if (matcher.find()) {
            String digit = matcher.group(1); // 个位数 "1"
            String extension = matcher.group(2); // ".mp4"
            // 替换成 -0数字 + 扩展名
            return oldName.replaceFirst("-" + digit + "\\" + extension, "-0" + digit + extension);
        }
        return oldName; // 不匹配则不变
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