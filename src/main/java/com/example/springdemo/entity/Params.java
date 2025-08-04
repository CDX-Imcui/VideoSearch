package com.example.springdemo.entity;

import lombok.Data;

@Data
public class Params {
    private Integer id;
    private String name;
    private String author;
    private String phone;
    private String major;
    private String classname;
    private Integer pageNum;
    private Integer pageSize;
}
