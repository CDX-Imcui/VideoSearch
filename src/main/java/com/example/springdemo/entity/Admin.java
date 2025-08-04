package com.example.springdemo.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * lombok 在编译时自动生成getter/setter方法以及toString、hashCode、equals等方法
 * 需要在pom.xml中引入lombok依赖，并在实体类上加上@Data注解
 */
@Data
@Table(name = "admin")
public class Admin {
    @Id
    private Integer id;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;
    @Column(name = "lastLoginTime")
    private LocalDateTime lastLoginTime;
    @Column(name = "role")
    private String role;

    @Transient//表示该字段不是数据库表中的字段
    private String token;

}
