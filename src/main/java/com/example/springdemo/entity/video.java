package com.example.springdemo.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Table(name = "video")
public class video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "uploader_id")
    private Integer uploader_id;
    @Column(name = "upload_time")
    private Integer upload_time;
    @Column(name = "video_guid")
    private String video_guid;
    @Column(name = "video_name")
    private String video_name;
    @Column(name = "file_size")
    private Integer file_size;
    @Column(name = "description_info")
    private Integer description_info;
    @Column(name = "finish_time")
    private Integer finish_time;
    @Column(name = "status")
    private Integer status;
    @Column(name = "remarks")
    private Integer remarks;
    @Transient               //表示该字段不是数据库表中的字段
    private String username; // 上传者的用户名
}
