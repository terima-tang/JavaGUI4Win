import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class MariaInitializer {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3307/mysql?useSSL=false";
        String username = "root"; // 默认用户名
        String password = ""; // 默认无密码
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE DATABASE IF NOT EXISTS testdb");
            stmt.execute("USE testdb");
            // 创建用户表
            String userTable = "CREATE TABLE IF NOT EXISTS user (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(50) NOT NULL)";
            stmt.execute(userTable);

            // 创建学生表
            String studentTable = "CREATE TABLE IF NOT EXISTS student (" +
                    "id INT PRIMARY KEY," +
                    "name VARCHAR(50) NOT NULL," +
                    "gender VARCHAR(10) NOT NULL," +
                    "age INT NOT NULL," +
                    "class_name VARCHAR(50) NOT NULL)";
            stmt.execute(studentTable);

            // 创建课程表
            String courseTable = "CREATE TABLE IF NOT EXISTS course (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "name VARCHAR(50) NOT NULL UNIQUE)";
            stmt.execute(courseTable);

            // 创建成绩表
            String gradeTable = "CREATE TABLE IF NOT EXISTS grade (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "student_id INT NOT NULL," +
                    "course_id INT NOT NULL," +
                    "score DOUBLE NOT NULL," +
                    "FOREIGN KEY (student_id) REFERENCES student(id)," +
                    "FOREIGN KEY (course_id) REFERENCES course(id)," +
                    "UNIQUE KEY (student_id, course_id))";
            stmt.execute(gradeTable);

            // 添加默认管理员用户
            stmt.execute("INSERT IGNORE INTO user (username, password) VALUES ('admin', 'admin')");

            System.out.println("数据库初始化成功！");
        } catch (SQLException e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
