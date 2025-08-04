package com.example.springdemo.dao;

import com.example.springdemo.entity.video;
import com.example.springdemo.entity.Params;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


@Repository
public interface VideoDao extends Mapper<video> {


    List<video> search(@Param("params") Params params);
}
