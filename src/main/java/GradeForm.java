import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GradeForm extends JDialog {
    private JTextField studentIdField;
    private JComboBox<String> courseComboBox;
    private JTextField scoreField;
    private Grade grade;

    public GradeForm(Frame parent, Grade grade) {
        super(parent, true);
        this.grade = grade;

        setTitle(grade == null ? "添加成绩" : "编辑成绩");
        setSize(300, 250);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panel.add(new JLabel("学生ID:"));
        studentIdField = new JTextField();
        panel.add(studentIdField);

        panel.add(new JLabel("课程:"));
        courseComboBox = new JComboBox<>();
        panel.add(courseComboBox);

        panel.add(new JLabel("成绩:"));
        scoreField = new JTextField();
        panel.add(scoreField);

        // 加载课程数据
        loadCourses();

        // 填充编辑数据
        if (grade != null) {
            studentIdField.setText(String.valueOf(grade.getStudentId()));
            courseComboBox.setSelectedItem(grade.getCourseName());
            scoreField.setText(String.valueOf(grade.getScore()));
        }

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGrade();
            }
        });

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        panel.add(saveButton);
        panel.add(cancelButton);

        add(panel);
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

            // 如果没有课程，添加一些默认课程
            if (courseComboBox.getItemCount() == 0) {
                courseComboBox.addItem("数学");
                courseComboBox.addItem("语文");
                courseComboBox.addItem("英语");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载课程失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // 保存成绩
    private void saveGrade() {
        try {
            int studentId = Integer.parseInt(studentIdField.getText());
            String courseName = (String) courseComboBox.getSelectedItem();
            double score = Double.parseDouble(scoreField.getText());

            if (score < 0 || score > 100) {
                JOptionPane.showMessageDialog(this, "成绩必须在0-100之间！");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                // 获取课程ID
                String courseSql = "SELECT id FROM course WHERE name = ?";
                PreparedStatement courseStmt = conn.prepareStatement(courseSql);
                courseStmt.setString(1, courseName);
                ResultSet rs = courseStmt.executeQuery();

                int courseId;
                if (rs.next()) {
                    courseId = rs.getInt("id");
                } else {
                    // 如果课程不存在则创建
                    String insertCourseSql = "INSERT INTO course (name) VALUES (?)";
                    PreparedStatement insertStmt = conn.prepareStatement(insertCourseSql, PreparedStatement.RETURN_GENERATED_KEYS);
                    insertStmt.setString(1, courseName);
                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    generatedKeys.next();
                    courseId = generatedKeys.getInt(1);
                }

                // 保存成绩
                String sql;
                if (grade == null) {
                    sql = "INSERT INTO grade (student_id, course_id, score) VALUES (?, ?, ?)";
                } else {
                    sql = "UPDATE grade SET student_id = ?, course_id = ?, score = ? WHERE id = ?";
                }

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, studentId);
                pstmt.setInt(2, courseId);
                pstmt.setDouble(3, score);

                if (grade != null) {
                    pstmt.setInt(4, grade.getId());
                }

                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "保存成功！");
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "请输入正确的数字格式！");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "数据库错误: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
