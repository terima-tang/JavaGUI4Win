
import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


    public class Maria {
        public static void init(){
            // 配置数据库（端口、数据目录等）
            DBConfigurationBuilder config = DBConfigurationBuilder.newBuilder();
            config.setPort(3307); // 端口（避免与本地已安装的 MySQL/MariaDB 冲突）
            config.setDataDir("mydb-data"); // 数据库文件存储目录（自动创建）

            // 启动数据库服务
            DB db = null;
            try {
                db = DB.newEmbeddedDB(config.build());
            } catch (ManagedProcessException e) {
                throw new RuntimeException(e);
            }
            try {
                db.start();
            } catch (ManagedProcessException e) {
                throw new RuntimeException(e);
            }

            // 此时可通过 JDBC 连接数据库（URL 格式：jdbc:mysql://localhost:端口/数据库名）
            String jdbcUrl = "jdbc:mysql://localhost:3307/mysql?useSSL=false";
            String username = "root"; // 默认用户名
            String password = ""; // 默认无密码

            // 后续可执行 SQL 操作（创建表、插入数据等）
            System.out.println("数据库启动成功，JDBC 连接 URL：" + jdbcUrl);

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



            // 程序结束时停止服务（可选，若不停止，数据会保存在 dataDir 中）
            // db.stop();
        }
    }










