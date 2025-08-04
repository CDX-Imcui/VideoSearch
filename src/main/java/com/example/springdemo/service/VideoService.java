package com.example.springdemo.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.springdemo.dao.AdminDao;
import com.example.springdemo.dao.VideoDao;
import com.example.springdemo.entity.Admin;
import com.example.springdemo.entity.video;
import com.example.springdemo.entity.Params;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class VideoService {

    @Resource
    private VideoDao videoDao;
    @Resource
    private AdminDao adminDao;

    public PageInfo<video> search(Params params) {
        //开启分页查询
        PageHelper.startPage(params.getPageNum(), params.getPageSize());
        //接下来的查询会被分页
        List<video> list = videoDao.search(params);
        if (!list.isEmpty()) {
            for (video v : list)
                if (ObjectUtil.isNotNull(v.getUploader_id())) {
                    Admin admin = adminDao.selectById(v.getUploader_id());
                    if (admin != null && admin.getUsername() != null)
                        v.setUsername(admin.getUsername());
                }
        }
        return PageInfo.of(list);
    }

    public void add(video v) {
        videoDao.insertSelective(v);
    }

    public void deleteById(Integer id) {
        // 获取要删除的信息，以获取文件标识
        video v = videoDao.selectByPrimaryKey(id);
        if (v != null && v.getVideo_guid() != null) {
            // 删除文件
            String filePath = System.getProperty("user.dir") + "/file_video/";
            List<String> filesNames = FileUtil.listFileNames(filePath);
            String fileToDelete = filesNames.stream()
                    .filter(name -> name.contains(v.getVideo_guid()))
                    .findAny()
                    .orElse("");

            if (StrUtil.isNotEmpty(fileToDelete)) {
                FileUtil.del(filePath + fileToDelete);
            }
        }

        videoDao.deleteByPrimaryKey(id);
    }

    public void update(video v) {
        videoDao.updateByPrimaryKeySelective(v);
    }

    public video findById(Integer id) {
        return videoDao.selectByPrimaryKey(id);
    }
}
