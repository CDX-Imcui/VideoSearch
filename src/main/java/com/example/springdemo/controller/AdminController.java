package com.example.springdemo.controller;

import com.example.springdemo.common.Result;
import com.example.springdemo.entity.Admin;
import com.example.springdemo.entity.Params;
import com.example.springdemo.service.AdminService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin // !允许跨域请求，否则前端无法访问
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired//自动注入对象，不需要new，否则会报空指针异常
    private AdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        Admin admin1 = adminService.login(admin);
        return Result.success(admin1);
    }

    @PostMapping("/register")
    public Result register(@RequestBody Admin admin) {
        adminService.addAdmin(admin);
        return Result.success();
    }


    @RequestMapping("/searchAdmin")
    public Result searchAdmin(Params params) {
        PageInfo<Admin> info = adminService.searchAdmin(params);
        return Result.success(info);
    }

    @RequestMapping("/majors")
    public Result getAllMajors() {
        List<String> info = adminService.getAllMajors();
        return Result.success(info);
    }
    @RequestMapping("/classnameList")
    public Result getClassnameList() {
        List<String> info = adminService.getClassnameList();
        return Result.success(info);
    }


    @PostMapping("/addAdmin")
    public Result addAdmin(@RequestBody Admin admin) {//@RequestBody会将请求体中的JSON数据转换为Admin对象
        adminService.addAdmin(admin);
        return Result.success();
    }

    @DeleteMapping("/deleteAdmin/{id}")
    public Result deleteAdmin(@PathVariable Integer id) {//@PathVariable会将URL中的id参数绑定到方法的参数id上
        adminService.deleteAdminById(id);
        return Result.success();
    }

    @PostMapping("/updateAdmin")
    public Result updateAdmin(@RequestBody Admin admin) {
        adminService.updateAdmin(admin);
        System.out.println(admin);
        return Result.success();
    }

}
