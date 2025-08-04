package com.example.springdemo.controller;

import com.example.springdemo.common.Result;
import com.example.springdemo.entity.video;
import com.example.springdemo.entity.Params;
import com.example.springdemo.service.VideoService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@CrossOrigin // !允许跨域请求，否则前端无法访问
@RestController
@RequestMapping("/book")
public class BookController {
    @Resource//自动注入对象，不需要new
    private VideoService bookService;

    @RequestMapping("/search")
    public Result search(Params params) {
        PageInfo<video> info = bookService.search(params);
        return Result.success(info);
    }

    @PostMapping("/add")
    public Result add(@RequestBody video book) {//@RequestBody会将请求体中的JSON数据转换为Admin对象
        bookService.add(book);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {//@PathVariable会将URL中的id参数绑定到方法的参数id上
        bookService.deleteById(id);
        return Result.success();
    }

    @PostMapping("/update")
    public Result update(@RequestBody video book) {
        bookService.update(book);
        return Result.success();
    }


}
