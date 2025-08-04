package com.example.springdemo.dao;

import com.example.springdemo.entity.Admin;
import com.example.springdemo.entity.Params;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface AdminDao extends Mapper<Admin> {
    @Select("SELECT * FROM admin WHERE id = #{id}")
//基于注解的方式
    Admin selectById(@Param("id") Integer id);

    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin selectByUsername(@Param("username") String username);


    List<Admin> searchAdmin(@Param("params") Params params);//基于xml的方式。@Param("username")注解给参数起别名，否则参数名默认为arg0,arg1...

    @Update("UPDATE admin SET lastLoginTime = NOW() WHERE id = #{id}")
    void updateLoginTime(@Param("id") Integer id);

    @Select("SELECT DISTINCT major FROM admin WHERE major IS NOT NULL")
    List<String> getAllMajors();

    @Select("SELECT DISTINCT classname FROM admin WHERE classname IS NOT NULL")
    List<String> getClassnameList();
}
