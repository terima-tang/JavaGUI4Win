import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GradeQueryFrame extends JFrame {
    private JTextField studentIdField;
    private JTextField studentNameField;
    private JComboBox<String> courseComboBox;
    private JComboBox<String> sortComboBox;
    private JTextArea resultArea;

    public GradeQueryFrame() {
        setTitle("成绩查询");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建查询条件面板
        JPanel criteriaPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        criteriaPanel.setBorder(BorderFactory.createTitledBorder("查询条件"));

        criteriaPanel.add(new JLabel("学生ID:"));
        studentIdField = new JTextField();
        criteriaPanel.add(studentIdField);

        criteriaPanel.add(new JLabel("学生姓名:"));
        studentNameField = new JTextField();
        criteriaPanel.add(studentNameField);

        criteriaPanel.add(new JLabel("课程:"));
        courseComboBox = new JComboBox<>();
        courseComboBox.addItem("所有课程");
        criteriaPanel.add(courseComboBox);

        criteriaPanel.add(new JLabel("排序方式:"));
        sortComboBox = new JComboBox<>();
        sortComboBox.addItem("按学生ID排序");
        sortComboBox.addItem("按成绩升序");
        sortComboBox.addItem("按成绩降序");
        criteriaPanel.add(sortComboBox);

        // 加载课程数据
        loadCourses();

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton queryButton = new JButton("查询");
        JButton clearButton = new JButton("清空");

        buttonPanel.add(queryButton);
        buttonPanel.add(clearButton);

        // 结果显示区域
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("查询结果"));

        // 布局设置
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(criteriaPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // 查询按钮事件
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performQuery();
            }
        });

        // 清空按钮事件
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });

        setVisible(true);
    }

    // 加载课程列表
    private void loadCourses() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM course";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                courseComboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载课程失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 执行查询
    private void performQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT g.id, s.id AS student_id, s.name AS student_name, ");
        sql.append("c.name AS course_name, g.score ");
        sql.append("FROM grade g ");
        sql.append("JOIN student s ON g.student_id = s.id ");
        sql.append("JOIN course c ON g.course_id = c.id ");
        sql.append("WHERE 1=1");

        // 构建查询条件
        if (!studentIdField.getText().trim().isEmpty()) {
            sql.append(" AND s.id = ?");
        }
        if (!studentNameField.getText().trim().isEmpty()) {
            sql.append(" AND s.name LIKE ?");
        }
        String course = (String) courseComboBox.getSelectedItem();
        if (!"所有课程".equals(course)) {
            sql.append(" AND c.name = ?");
        }

        // 排序条件
        String sort = (String) sortComboBox.getSelectedItem();
        if ("按学生ID排序".equals(sort)) {
            sql.append(" ORDER BY s.id");
        } else if ("按成绩升序".equals(sort)) {
            sql.append(" ORDER BY g.score ASC");
        } else if ("按成绩降序".equals(sort)) {
            sql.append(" ORDER BY g.score DESC");
        }

        // 执行查询并显示结果
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            // 设置查询参数
            if (!studentIdField.getText().trim().isEmpty()) {
                pstmt.setInt(paramIndex++, Integer.parseInt(studentIdField.getText().trim()));
            }
            if (!studentNameField.getText().trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + studentNameField.getText().trim() + "%");
            }
            if (!"所有课程".equals(course)) {
                pstmt.setString(paramIndex++, course);
            }

            ResultSet rs = pstmt.executeQuery();

            resultArea.setText(""); // 清空之前的结果
            resultArea.append(String.format("%-6s %-10s %-15s %-15s %-6s%n",
                    "ID", "学生ID", "学生姓名", "课程", "成绩"));
            resultArea.append("------------------------------------------------------------\n");

            boolean hasResult = false;
            while (rs.next()) {
                hasResult = true;
                resultArea.append(String.format("%-6d %-10d %-15s %-15s %-6.1f%n",
                        rs.getInt("id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("course_name"),
                        rs.getDouble("score")));
            }

            if (!hasResult) {
                resultArea.append("没有找到匹配的记录");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入正确的数字格式！");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "查询失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // 清空输入字段
    private void clearFields() {
        studentIdField.setText("");
        studentNameField.setText("");
        courseComboBox.setSelectedIndex(0);
        sortComboBox.setSelectedIndex(0);
        resultArea.setText("");
    }
}
    