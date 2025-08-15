import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StudentForm extends JDialog {
    private JTextField idField;
    private JTextField nameField;
    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;
    private JTextField ageField;
    private JTextField classNameField;
    private Student student;
    
    public StudentForm(Frame parent, Student student) {
        super(parent, true);
        this.student = student;
        
        setTitle(student == null ? "添加学生" : "编辑学生");
        setSize(300, 300);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("学号:"));
        idField = new JTextField();
        idField.setEditable(student == null); // 编辑时学号不可改
        panel.add(idField);
        
        panel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        panel.add(nameField);
        
        panel.add(new JLabel("性别:"));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maleRadio = new JRadioButton("男", true);
        femaleRadio = new JRadioButton("女");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        panel.add(genderPanel);
        
        panel.add(new JLabel("年龄:"));
        ageField = new JTextField();
        panel.add(ageField);
        
        panel.add(new JLabel("班级:"));
        classNameField = new JTextField();
        panel.add(classNameField);
        
        // 填充编辑数据
        if (student != null) {
            idField.setText(String.valueOf(student.getId()));
            nameField.setText(student.getName());
            if ("女".equals(student.getGender())) {
                femaleRadio.setSelected(true);
            }
            ageField.setText(String.valueOf(student.getAge()));
            classNameField.setText(student.getClassName());
        }
        
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveStudent();
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
    
    private void saveStudent() {
        try {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            String gender = maleRadio.isSelected() ? "男" : "女";
            int age = Integer.parseInt(ageField.getText());
            String className = classNameField.getText();
            
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                if (student == null) {
                    sql = "INSERT INTO student (id, name, gender, age, class_name) VALUES (?, ?, ?, ?, ?)";
                } else {
                    sql = "UPDATE student SET name = ?, gender = ?, age = ?, class_name = ? WHERE id = ?";
                }
                
                PreparedStatement pstmt = conn.prepareStatement(sql);
                if (student == null) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, name);
                    pstmt.setString(3, gender);
                    pstmt.setInt(4, age);
                    pstmt.setString(5, className);
                } else {
                    pstmt.setString(1, name);
                    pstmt.setString(2, gender);
                    pstmt.setInt(3, age);
                    pstmt.setString(4, className);
                    pstmt.setInt(5, id);
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
