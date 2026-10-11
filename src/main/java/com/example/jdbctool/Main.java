package com.example.jdbctool;

import com.example.jdbctool.entity.College;
import com.example.jdbctool.entity.Student;
import com.example.jdbctool.util.JDBCTool;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * 演示程序：使用 JDBCTool 对 DateTest 库中的 student / college 两张表做增删改查。
 *
 * 运行方式：
 *   mvn clean compile
 *   mvn exec:java -Dexec.mainClass=com.example.jdbctool.Main
 */
public class Main {

    public static void main(String[] args) {
        Connection conn = null;

        try {
            Properties props = new Properties();
            InputStream in = Main.class.getClassLoader().getResourceAsStream("db.properties");

            if (in == null) {
                throw new RuntimeException("db.properties 未找到");
            }

            props.load(in);

            Class.forName(props.getProperty("jdbc.driver"));

            conn = DriverManager.getConnection(
                    props.getProperty("jdbc.url"),
                    props.getProperty("jdbc.username"),
                    props.getProperty("jdbc.password")
            );

            conn.setAutoCommit(false);

            // 清理旧数据，方便重复运行
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM student");
                st.executeUpdate("DELETE FROM college");
            }
            conn.commit();

            System.out.println("==================== 学院表 college 增删改查 ====================");

            // ---------- 增 ----------
            College college = new College("C001", "计算机学院", "CS");
            System.out.println("[增] 新增学院影响行数：" + JDBCTool.save(college, conn));
            JDBCTool.save(new College("C002", "外国语学院", "FL"), conn);

            // ---------- 查（单个） ----------
            College collegeDb = JDBCTool.getOneById("C001", College.class, conn);
            System.out.println("[查] 按主键查询学院：" + collegeDb);

            // ---------- 改 ----------
            collegeDb.setName("计算机与人工智能学院");
            System.out.println("[改] 更新学院影响行数：" + JDBCTool.update(collegeDb, conn));
            System.out.println("[查] 更新后再查询：" + JDBCTool.getOneById("C001", College.class, conn));

            // ---------- 查（列表，演示方法1 resultSetToList） ----------
            System.out.println("[查] 学院列表（方法一 resultSetToList）：");
            for (College c : queryAll(conn, "SELECT * FROM college", College.class)) {
                System.out.println("      " + c);
            }

            // ---------- 删 ----------
            College delCollege = new College();
            delCollege.setId("C002");
            System.out.println("[删] 删除学院 C002 影响行数：" + JDBCTool.delete(delCollege, conn));

            System.out.println();
            System.out.println("==================== 学生表 student 增删改查 ====================");

            // ---------- 增 ----------
            Student student = new Student(
                    "S001",
                    "张三",
                    "计算机科学与技术",
                    18,
                    Date.valueOf("2024-09-01"),
                    false,
                    new BigDecimal("5000.00")
            );
            System.out.println("[增] 新增学生影响行数：" + JDBCTool.save(student, conn));

            JDBCTool.save(new Student("S002", "李四", "软件工程", 20,
                    Date.valueOf("2023-09-01"), true, new BigDecimal("5200.00")), conn);
            JDBCTool.save(new Student("S003", "王五", "网络工程", 19,
                    Date.valueOf("2024-09-01"), false, new BigDecimal("4800.00")), conn);

            // ---------- 查（单个） ----------
            Student studentDb = JDBCTool.getOneById("S001", Student.class, conn);
            System.out.println("[查] 按主键查询学生：" + studentDb);

            // ---------- 改 ----------
            studentDb.setTuition(new BigDecimal("5200.00"));
            studentDb.setGraduated(true);
            System.out.println("[改] 更新学生影响行数：" + JDBCTool.update(studentDb, conn));
            System.out.println("[查] 更新后再查询：" + JDBCTool.getOneById("S001", Student.class, conn));

            // ---------- 查（列表，演示方法1 resultSetToList） ----------
            System.out.println("[查] 学生列表（方法一 resultSetToList）：");
            for (Student s : queryAll(conn, "SELECT * FROM student", Student.class)) {
                System.out.println("      " + s);
            }

            // ---------- 删 ----------
            Student delStudent = new Student();
            delStudent.setId("S003");
            System.out.println("[删] 删除学生 S003 影响行数：" + JDBCTool.delete(delStudent, conn));
            System.out.println("[查] 删除后剩余学生数：" + queryAll(conn, "SELECT * FROM student", Student.class).size());

            conn.commit();
            System.out.println();
            System.out.println("事务已提交。");

            System.out.println();
            System.out.println("==================== 提交后库中最终数据 ====================");
            for (College c : queryAll(conn, "SELECT * FROM college", College.class)) {
                System.out.println("      " + c);
            }
            for (Student s : queryAll(conn, "SELECT * FROM student", Student.class)) {
                System.out.println("      " + s);
            }

        } catch (Exception e) {
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 全表 / 条件查询：直接用 JDBCTool 的方法一（resultSetToList）把结果集转成对象列表。
     */
    private static <T> List<T> queryAll(Connection conn, String sql, Class<T> clazz) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return JDBCTool.resultSetToList(rs, clazz);
        }
    }
}
