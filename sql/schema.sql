-- =====================================================================
--  作业：JDBCTool 工具类演示 —— 数据库脚本
--  数据库：DateTest
--  说明：本脚本可重复执行（幂等），用于初始化两张表
--        1) student 学生表：Id, name, major, age, 入学时间, 是否毕业, 学费
--        2) college 学院表：id, name, code
-- =====================================================================

CREATE DATABASE IF NOT EXISTS DateTest
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE DateTest;

DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS college;

-- ---------------------------------------------------------------------
-- 学院表
-- ---------------------------------------------------------------------
CREATE TABLE college (
    id   VARCHAR(20)  NOT NULL COMMENT '学院编号（主键）',
    name VARCHAR(100) NOT NULL COMMENT '学院名称',
    code VARCHAR(50)  NOT NULL COMMENT '学院代码',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学院表';

-- ---------------------------------------------------------------------
-- 学生表
-- ---------------------------------------------------------------------
CREATE TABLE student (
    id          VARCHAR(20)   NOT NULL COMMENT '学号（主键）',
    name        VARCHAR(100)  NOT NULL COMMENT '姓名',
    major       VARCHAR(100)  DEFAULT NULL COMMENT '专业',
    age         INT           DEFAULT NULL COMMENT '年龄',
    enroll_date DATE          DEFAULT NULL COMMENT '入学时间',
    graduated   TINYINT(1)    DEFAULT 0    COMMENT '是否毕业 0-未毕业 1-已毕业',
    tuition     DECIMAL(10,2) DEFAULT NULL COMMENT '学费',
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '学生表';

-- ---------------------------------------------------------------------
-- 初始化数据
-- ---------------------------------------------------------------------
INSERT INTO college (id, name, code) VALUES
    ('C001', '计算机学院', 'CS'),
    ('C002', '外国语学院', 'FL');

INSERT INTO student (id, name, major, age, enroll_date, graduated, tuition) VALUES
    ('S001', '张三', '计算机科学与技术', 18, '2024-09-01', 0, 5000.00),
    ('S002', '李四', '软件工程',         19, '2023-09-01', 1, 5200.00);

-- 验证
SELECT * FROM college;
SELECT * FROM student;