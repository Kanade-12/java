# jdbctool-demo

> JDBC + Java 反射实现的**简易 ORM 工具类 `JDBCTool`**，并对 `DateTest` 库中的 `student`（学生表）与 `college`（学院表）完成增删改查演示。

---

## 一、环境要求

| 组件 | 版本 |
| --- | --- |
| JDK | 8 及以上（本项目在 JDK 17 下验证通过） |
| Maven | 3.6+（本项目在 Maven 3.9.11 下验证通过） |
| MySQL | 8.x（本项目在 MySQL 8.4.7 下验证通过） |

---

## 二、数据库准备

执行 [`sql/schema.sql`](sql/schema.sql)，它会创建数据库 `DateTest` 以及两张表：

```bash
mysql -uroot -p < sql/schema.sql
```

**学生表 `student`**

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| id | varchar(20) PK | 学号 |
| name | varchar(100) | 姓名 |
| major | varchar(100) | 专业 |
| age | int | 年龄 |
| enroll_date | date | 入学时间 |
| graduated | tinyint(1) | 是否毕业 |
| tuition | decimal(10,2) | 学费 |

**学院表 `college`**

| 字段 | 类型 | 含义 |
| --- | --- | --- |
| id | varchar(20) PK | 学院编号 |
| name | varchar(100) | 学院名称 |
| code | varchar(50) | 学院代码 |

数据库连接参数在 [`src/main/resources/db.properties`](src/main/resources/db.properties) 中配置，**请按自己的环境修改用户名和密码**。

---

## 三、项目结构

```
jdbctool-demo
├── pom.xml                          Maven 构建文件
├── sql/schema.sql                   建库建表脚本
├── docs/                            作业答案与运行截图
└── src/main
    ├── java/com/example/jdbctool
    │   ├── Main.java                演示程序
    │   ├── annotation/              @Table / @Column / @Id
    │   ├── entity/                  Student / College
    │   └── util/JDBCTool.java       核心工具类
    └── resources/db.properties      数据库连接配置
```

---

## 四、核心类 JDBCTool

| 方法 | 说明 |
| --- | --- |
| `static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz)` | 把 `ResultSet` 转换成对象列表，字段与列名一一对应 |
| `static <T> int save(T obj, Connection connection)` | 插入一条记录 |
| `static <T> int update(T obj, Connection connection)` | 按主键更新记录 |
| `static <T> int delete(T obj, Connection connection)` | 按主键删除记录 |
| `static <T> T getOneById(String id, Class<T> clazz, Connection connection)` | 按主键查询单个对象 |

内部辅助方法：`getTableName`、`getColumnName`、`getIdField`、`getMappedFields`、`getColumnFieldMap`、`setParameters`、`setFieldValue`、`convertValue`。

### 实现原理

1. **注解映射**：实体类用 `@Table("表名")` 标注、字段用 `@Column("列名")` 标注、主键用 `@Id` 标注。
2. **反射建对象**：`clazz.getDeclaredConstructor().newInstance()` 创建实例。
3. **反射赋值**：`field.setAccessible(true)` 后 `field.set(obj, value)`，并按目标字段类型做类型转换（`Integer` / `BigDecimal` / `java.sql.Date` / `Boolean` 等）。
4. **反射生成 SQL**：遍历字段拼出 `INSERT` / `UPDATE` / `DELETE` / `SELECT` 语句，全部使用 `PreparedStatement` 占位符，避免 SQL 注入。

---

## 五、运行方式

```bash
# 1. 编译
mvn clean compile

# 2. 运行演示程序
mvn exec:java -Dexec.mainClass=com.example.jdbctool.Main
```

> 在 IDE 中直接运行 `com.example.jdbctool.Main` 的 `main` 方法即可。

---

## 六、运行效果

![运行效果](docs/screenshots/03-run-result.png)

更多截图见 [`docs/screenshots/`](docs/screenshots)。

---

## 七、版本管理

```bash
git clone https://github.com/Kanade-12/java.git
```
