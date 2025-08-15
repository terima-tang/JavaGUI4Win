import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("学生成绩管理系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();
        
        // 学生管理菜单
        JMenu studentMenu = new JMenu("学生管理");
        studentMenu.add(createMenuItem("添加学生", e -> new StudentForm(this, null)));
        studentMenu.add(createMenuItem("查询学生", e -> new StudentQueryFrame()));
        menuBar.add(studentMenu);
        
        // 成绩管理菜单
        JMenu gradeMenu = new JMenu("成绩管理");
        gradeMenu.add(createMenuItem("添加成绩", e -> new GradeForm(this, null)));
        gradeMenu.add(createMenuItem("查询成绩", e -> new GradeQueryFrame()));
        menuBar.add(gradeMenu);
        
        // 统计分析菜单
        JMenu statsMenu = new JMenu("统计分析");
        statsMenu.add(createMenuItem("成绩统计", e -> new StatisticsFrame()));
        menuBar.add(statsMenu);
        
        // 设置菜单栏
        setJMenuBar(menuBar);
        
        // 初始显示欢迎信息
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("欢迎使用学生成绩管理系统", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("宋体", Font.BOLD, 24));
        welcomePanel.add(welcomeLabel, BorderLayout.CENTER);
        add(welcomePanel);
        
        setVisible(true);
    }
    
    private JMenuItem createMenuItem(String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }
}
