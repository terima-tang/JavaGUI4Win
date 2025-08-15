import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Maria.init();
        // 确保GUI在事件调度线程中运行
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }
}
