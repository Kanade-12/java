package com.example.jdbctool.entity;

import com.example.jdbctool.annotation.Column;
import com.example.jdbctool.annotation.Id;
import com.example.jdbctool.annotation.Table;

import java.math.BigDecimal;
import java.sql.Date;

@Table("student")
public class Student {

    @Id
    @Column("id")
    private String id;

    @Column("name")
    private String name;

    @Column("major")
    private String major;

    @Column("age")
    private Integer age;

    @Column("enroll_date")
    private Date enrollDate;

    @Column("graduated")
    private Boolean graduated;

    @Column("tuition")
    private BigDecimal tuition;

    public Student() {
    }

    public Student(String id, String name, String major, Integer age,
                   Date enrollDate, Boolean graduated, BigDecimal tuition) {
        this.id = id;
        this.name = name;
        this.major = major;
        this.age = age;
        this.enrollDate = enrollDate;
        this.graduated = graduated;
        this.tuition = tuition;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Date getEnrollDate() {
        return enrollDate;
    }

    public void setEnrollDate(Date enrollDate) {
        this.enrollDate = enrollDate;
    }

    public Boolean getGraduated() {
        return graduated;
    }

    public void setGraduated(Boolean graduated) {
        this.graduated = graduated;
    }

    public BigDecimal getTuition() {
        return tuition;
    }

    public void setTuition(BigDecimal tuition) {
        this.tuition = tuition;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", major='" + major + '\'' +
                ", age=" + age +
                ", enrollDate=" + enrollDate +
                ", graduated=" + graduated +
                ", tuition=" + tuition +
                '}';
    }
}