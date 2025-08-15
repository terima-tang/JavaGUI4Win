import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StatisticsFrame extends JFrame {
    private JComboBox<String> courseComboBox;
    private JTextArea resultArea;

    public StatisticsFrame() {
        setTitle("成绩统计分析");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建统计条件面板
        JPanel criteriaPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        criteriaPanel.setBorder(BorderFactory.createTitledBorder("统计条件"));

        criteriaPanel.add(new JLabel("选择课程:"));
        courseComboBox = new JComboBox<>();
        courseComboBox.addItem("所有课程");
        criteriaPanel.add(courseComboBox);

        JButton statsButton = new JButton("生成统计");
        statsButton.addActionListener(e -> generateStatistics());
        criteriaPanel.add(statsButton);

        // 加载课程数据
        loadCourses();

        // 结果显示区域
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("统计结果"));

        // 布局设置
        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(criteriaPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

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

    // 生成统计信息
    private void generateStatistics() {
        String course = (String) courseComboBox.getSelectedItem();
        StringBuilder result = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {
            // 基础SQL
            String baseSql = "SELECT g.score, s.class_name " +
                    "FROM grade g " +
                    "JOIN student s ON g.student_id = s.id " +
                    "JOIN course c ON g.course_id = c.id";

            // 添加课程条件
            String whereClause = "";
            if (!"所有课程".equals(course)) {
                whereClause = " WHERE c.name = ?";
            }

            // 1. 总人数和平均分
            String avgSql = "SELECT COUNT(DISTINCT g.student_id) AS total_students, " +
                    "AVG(g.score) AS avg_score, " +
                    "MAX(g.score) AS max_score, " +
                    "MIN(g.score) AS min_score " +
                    "FROM grade g " +
                    "JOIN course c ON g.course_id = c.id" + whereClause;

            PreparedStatement avgStmt = conn.prepareStatement(avgSql);
            if (!"所有课程".equals(course)) {
                avgStmt.setString(1, course);
            }
            ResultSet avgRs = avgStmt.executeQuery();

            if (avgRs.next()) {
                int total = avgRs.getInt("total_students");
                double avg = avgRs.getDouble("avg_score");
                double max = avgRs.getDouble("max_score");
                double min = avgRs.getDouble("min_score");

                result.append("=== 总体统计 ===\n");
                result.append("课程: ").append(course).append("\n");
                result.append("总人数: ").append(total).append("\n");
                result.append("平均分: ").append(String.format("%.1f", avg)).append("\n");
                result.append("最高分: ").append(String.format("%.1f", max)).append("\n");
                result.append("最低分: ").append(String.format("%.1f", min)).append("\n\n");
            }

            // 2. 及格率统计
            String passSql = "SELECT " +
                    "(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS pass_rate " +
                    "FROM grade g " +
                    "JOIN course c ON g.course_id = c.id" + whereClause;

            PreparedStatement passStmt = conn.prepareStatement(passSql);
            if (!"所有课程".equals(course)) {
                passStmt.setString(1, course);
            }
            ResultSet passRs = passStmt.executeQuery();

            if (passRs.next()) {
                double passRate = passRs.getDouble("pass_rate");
                result.append("及格率: ").append(String.format("%.1f", passRate)).append("%\n\n");
            }

            // 3. 分数段统计
            String rangeSql = "SELECT " +
                    "SUM(CASE WHEN g.score >= 90 THEN 1 ELSE 0 END) AS a_count, " +
                    "SUM(CASE WHEN g.score >= 80 AND g.score < 90 THEN 1 ELSE 0 END) AS b_count, " +
                    "SUM(CASE WHEN g.score >= 70 AND g.score < 80 THEN 1 ELSE 0 END) AS c_count, " +
                    "SUM(CASE WHEN g.score >= 60 AND g.score < 70 THEN 1 ELSE 0 END) AS d_count, " +
                    "SUM(CASE WHEN g.score < 60 THEN 1 ELSE 0 END) AS f_count " +
                    "FROM grade g " +
                    "JOIN course c ON g.course_id = c.id" + whereClause;

            PreparedStatement rangeStmt = conn.prepareStatement(rangeSql);
            if (!"所有课程".equals(course)) {
                rangeStmt.setString(1, course);
            }
            ResultSet rangeRs = rangeStmt.executeQuery();

            if (rangeRs.next()) {
                result.append("=== 分数段统计 ===\n");
                result.append("90分以上: ").append(rangeRs.getInt("a_count")).append("\n");
                result.append("80-89分: ").append(rangeRs.getInt("b_count")).append("\n");
                result.append("70-79分: ").append(rangeRs.getInt("c_count")).append("\n");
                result.append("60-69分: ").append(rangeRs.getInt("d_count")).append("\n");
                result.append("60分以下: ").append(rangeRs.getInt("f_count")).append("\n");
            }

            resultArea.setText(result.toString());

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "统计失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
