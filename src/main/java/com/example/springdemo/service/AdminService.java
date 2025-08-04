package com.example.springdemo.service;

import com.example.springdemo.common.JwtTokenUtils;
import com.example.springdemo.entity.Admin;
import com.example.springdemo.dao.AdminDao;
import com.example.springdemo.entity.Params;
import com.example.springdemo.exception.CustomException;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    @Autowired
    private AdminDao adminDao;

    public PageInfo<Admin> searchAdmin(Params params) {
        //开启分页查询
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        //接下来的查询会被分页
        List<Admin> list = adminDao.searchAdmin(params);
        return PageInfo.of(list);
    }


    public void addAdmin(Admin admin) {
        if (admin.getPassword() == null) {
            throw new CustomException("密码不能为空");
        }
        admin.setLastLoginTime(LocalDateTime.now());
//        Admin admin1 = adminDao.selectById(admin.getId());
        Admin admin1 = adminDao.selectByUsername(admin.getUsername());
        if (admin1 != null) {
            throw new CustomException("该用户已存在，请勿重复添加");
        }
        if (admin.getRole() == null) {
            admin.setRole("role_student");
        }
        adminDao.insertSelective(admin);
    }

    public void updateAdmin(Admin admin) {
        adminDao.updateByPrimaryKeySelective(admin);
    }

    public void deleteAdminById(Integer id) {
        adminDao.deleteByPrimaryKey(id);
    }

    public Admin login(Admin admin) {
//        Admin user = adminDao.selectById(admin.getId());
        Admin user = adminDao.selectByUsername(admin.getUsername());
        if (user == null) {
            throw new CustomException("用户不存在");
        } else if (!admin.getPassword().equals(user.getPassword())) {
            throw new CustomException("密码错误");
        }
        String token = JwtTokenUtils.generateToken(user.getId(), user.getPassword());
        user.setToken(token);//登录成功，将token带给前端
        //当前时间不必传给前端，前端显示的是上次时间，不是当前时间
        adminDao.updateLoginTime(user.getId());
        return user;
    }

    public Admin getAdminById(Integer id) {
        return adminDao.selectById(id);
    }

    public List<String> getAllMajors() {
        return adminDao.getAllMajors();
    }

    public List<String> getClassnameList() {
        return adminDao.getClassnameList();
    }
}
