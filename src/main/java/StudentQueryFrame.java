import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class StudentQueryFrame extends JFrame {
    private JTextField idField;
    private JTextField nameField;
    private JTextField classNameField;
    private JTable studentTable;
    private DefaultTableModel tableModel;

    public StudentQueryFrame() {
        setTitle("查询学生");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建查询条件面板
        JPanel queryPanel = new JPanel(new GridLayout(1, 6, 10, 10));
        queryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        queryPanel.add(new JLabel("学号:"));
        idField = new JTextField();
        queryPanel.add(idField);

        queryPanel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        queryPanel.add(nameField);

        queryPanel.add(new JLabel("班级:"));
        classNameField = new JTextField();
        queryPanel.add(classNameField);

        JButton queryButton = new JButton("查询");
        queryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                queryStudents();
            }
        });
        queryPanel.add(queryButton);

        // 创建表格模型
        String[] columnNames = {"学号", "姓名", "性别", "年龄", "班级", "操作"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 仅操作列可编辑
            }
        };

        studentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        // 添加组件
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(queryPanel, BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 初始加载所有学生
        queryStudents();

        setVisible(true);
    }

    private void queryStudents() {
        // 清空表格
        tableModel.setRowCount(0);

        try (Connection conn = DBConnection.getConnection()) {
            // 构建查询SQL
            StringBuilder sql = new StringBuilder("SELECT id, name, gender, age, class_name FROM student WHERE 1=1");
            Vector<Object> params = new Vector<>();

            // 添加查询条件
            if (!idField.getText().trim().isEmpty()) {
                sql.append(" AND id = ?");
                params.add(Integer.parseInt(idField.getText().trim()));
            }

            if (!nameField.getText().trim().isEmpty()) {
                sql.append(" AND name LIKE ?");
                params.add("%" + nameField.getText().trim() + "%");
            }

            if (!classNameField.getText().trim().isEmpty()) {
                sql.append(" AND class_name LIKE ?");
                params.add("%" + classNameField.getText().trim() + "%");
            }

            PreparedStatement pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = pstmt.executeQuery();

            // 填充表格
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("name"));
                row.add(rs.getString("gender"));
                row.add(rs.getInt("age"));
                row.add(rs.getString("class_name"));

                // 添加编辑按钮
                JButton editButton = new JButton("编辑");
                int studentId = rs.getInt("id");
                editButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        editStudent(studentId);
                    }
                });
                row.add(editButton);

                tableModel.addRow(row);
            }

            // 调整表格列宽
            for (int i = 0; i < studentTable.getColumnCount(); i++) {
                studentTable.getColumnModel().getColumn(i).setPreferredWidth(100);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "学号必须是数字！");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "查询失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void editStudent(int studentId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT id, name, gender, age, class_name FROM student WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Student student = new Student(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("gender"),
                        rs.getInt("age"),
                        rs.getString("class_name")
                );
                // 打开编辑窗口
                new StudentForm(this, student);
                // 编辑完成后刷新表格
                queryStudents();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "加载学生信息失败: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}