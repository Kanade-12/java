package com.example.jdbctool.util;

import com.example.jdbctool.annotation.Column;
import com.example.jdbctool.annotation.Id;
import com.example.jdbctool.annotation.Table;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class JDBCTool {

    /**
     * 方法1：ResultSet -> List<T>
     */
    public static <T> List<T> resultSetToList(ResultSet rs, Class<T> clazz) throws Exception {
        List<T> list = new ArrayList<>();
        Map<String, Field> columnFieldMap = getColumnFieldMap(clazz);

        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        while (rs.next()) {
            T obj = clazz.getDeclaredConstructor().newInstance();

            for (int i = 1; i <= columnCount; i++) {
                String columnLabel = meta.getColumnLabel(i);
                Field field = columnFieldMap.get(columnLabel.toLowerCase(Locale.ROOT));

                if (field != null) {
                    Object value = rs.getObject(i);
                    setFieldValue(obj, field, value);
                }
            }

            list.add(obj);
        }

        return list;
    }

    /**
     * 方法2：保存对象
     */
    public static <T> int save(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        List<Field> fields = getMappedFields(clazz);

        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Field field : fields) {
            field.setAccessible(true);
            columns.append(getColumnName(field)).append(",");
            placeholders.append("?,");
            params.add(field.get(obj));
        }

        columns.setLength(columns.length() - 1);
        placeholders.setLength(placeholders.length() - 1);

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * 方法3：更新对象
     */
    public static <T> int update(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);
        idField.setAccessible(true);

        Object idValue = idField.get(obj);
        if (idValue == null) {
            throw new IllegalArgumentException("主键不能为空");
        }

        List<Field> fields = getMappedFields(clazz);
        StringBuilder setSql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (Field field : fields) {
            if (field.getName().equals(idField.getName())) {
                continue;
            }

            field.setAccessible(true);
            setSql.append(getColumnName(field)).append("=?,");
            params.add(field.get(obj));
        }

        if (setSql.length() == 0) {
            throw new IllegalArgumentException("没有可更新字段");
        }

        setSql.setLength(setSql.length() - 1);

        String sql = "UPDATE " + tableName + " SET " + setSql +
                " WHERE " + getColumnName(idField) + "=?";

        params.add(idValue);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setParameters(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * 方法4：删除对象
     */
    public static <T> int delete(T obj, Connection connection) throws Exception {
        Class<?> clazz = obj.getClass();
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);
        idField.setAccessible(true);

        Object idValue = idField.get(obj);
        if (idValue == null) {
            throw new IllegalArgumentException("主键不能为空");
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + getColumnName(idField) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, idValue);
            return ps.executeUpdate();
        }
    }

    /**
     * 方法5：根据 id 查询一个对象
     */
    public static <T> T getOneById(String id, Class<T> clazz, Connection connection) throws Exception {
        String tableName = getTableName(clazz);
        Field idField = getIdField(clazz);

        String sql = "SELECT * FROM " + tableName + " WHERE " + getColumnName(idField) + "=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                List<T> list = resultSetToList(rs, clazz);
                return list.isEmpty() ? null : list.get(0);
            }
        }
    }

    // ================= 辅助方法 =================

    private static Map<String, Field> getColumnFieldMap(Class<?> clazz) {
        Map<String, Field> map = new HashMap<>();

        for (Field field : getMappedFields(clazz)) {
            map.put(getColumnName(field).toLowerCase(Locale.ROOT), field);
            map.put(field.getName().toLowerCase(Locale.ROOT), field);
        }

        return map;
    }

    private static List<Field> getMappedFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isTransient(field.getModifiers())
                        || field.isSynthetic()) {
                    continue;
                }
                list.add(field);
            }
            current = current.getSuperclass();
        }

        return list;
    }

    private static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null || table.value().trim().isEmpty()) {
            throw new IllegalArgumentException("类 " + clazz.getName() + " 缺少 @Table 注解");
        }
        return table.value();
    }

    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column != null && !column.value().trim().isEmpty()) {
            return column.value();
        }
        return field.getName();
    }

    private static Field getIdField(Class<?> clazz) {
        for (Field field : getMappedFields(clazz)) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }

        for (Field field : getMappedFields(clazz)) {
            if ("id".equalsIgnoreCase(field.getName())) {
                return field;
            }
        }

        throw new IllegalArgumentException("类 " + clazz.getName() + " 未找到主键字段");
    }

    private static void setParameters(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    private static void setFieldValue(Object obj, Field field, Object value) throws Exception {
        field.setAccessible(true);
        Class<?> targetType = field.getType();

        if (value == null) {
            if (!targetType.isPrimitive()) {
                field.set(obj, null);
            }
            return;
        }

        Object converted = convertValue(value, targetType);
        field.set(obj, converted);
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }

        String s = value.toString();

        if (targetType == String.class) {
            return s;
        }

        if (targetType == Integer.class || targetType == int.class) {
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(s);
        }

        if (targetType == Long.class || targetType == long.class) {
            return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(s);
        }

        if (targetType == Double.class || targetType == double.class) {
            return value instanceof Number ? ((Number) value).doubleValue() : Double.parseDouble(s);
        }

        if (targetType == Float.class || targetType == float.class) {
            return value instanceof Number ? ((Number) value).floatValue() : Float.parseFloat(s);
        }

        if (targetType == Short.class || targetType == short.class) {
            return value instanceof Number ? ((Number) value).shortValue() : Short.parseShort(s);
        }

        if (targetType == Byte.class || targetType == byte.class) {
            return value instanceof Number ? ((Number) value).byteValue() : Byte.parseByte(s);
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            if (value instanceof Number) {
                return ((Number) value).intValue() != 0;
            }
            return "1".equals(s) || "true".equalsIgnoreCase(s);
        }

        if (targetType == BigDecimal.class) {
            return new BigDecimal(s);
        }

        if (targetType == java.sql.Date.class) {
            if (value instanceof java.sql.Date) {
                return value;
            }
            if (value instanceof java.util.Date) {
                return new java.sql.Date(((java.util.Date) value).getTime());
            }
            if (value instanceof LocalDate) {
                return java.sql.Date.valueOf((LocalDate) value);
            }
            return java.sql.Date.valueOf(s);
        }

        if (targetType == java.util.Date.class) {
            if (value instanceof java.util.Date) {
                return value;
            }
            if (value instanceof LocalDate) {
                return java.sql.Date.valueOf((LocalDate) value);
            }
            if (value instanceof LocalDateTime) {
                return Timestamp.valueOf((LocalDateTime) value);
            }
            // 避免使用已过时的 Date(String) 构造器
            return s.contains(":") ? Timestamp.valueOf(s) : java.sql.Date.valueOf(s);
        }

        if (targetType == LocalDate.class) {
            if (value instanceof java.sql.Date) {
                return ((java.sql.Date) value).toLocalDate();
            }
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime().toLocalDate();
            }
            return LocalDate.parse(s);
        }

        if (targetType == LocalDateTime.class) {
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toLocalDateTime();
            }
            return LocalDateTime.parse(s);
        }

        return value;
    }
}