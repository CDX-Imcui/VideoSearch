package com.example.springdemo.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.example.springdemo.common.Result;
import com.example.springdemo.service.PollingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.core.io.Resource;

/*
 * 文件上传接口
 */
@RestController
@RequestMapping("/files")
public class FileController {
    //文件上传存储路径 根目录/file
    private static final String filePath = System.getProperty("user.dir") + "/file_video/";
    @Autowired
    private PollingService pollingService;
    /*
    //文件上传
     */
    @PostMapping("/upload")//上传视频，返回 flag
    public Result upload(MultipartFile file) {
        synchronized (FileController.class) {
            //时间戳
            String flag = System.currentTimeMillis() + "";
            //上传文件的名称
            String fileName = file.getOriginalFilename();
            try {
                if (!FileUtil.isDirectory(filePath)) {
                    FileUtil.mkdir(filePath);
                    System.out.println("文件夹创建成功：" + filePath);
                } else {
                    System.out.println("文件夹已存在：" + filePath);
                }
                //文件存储形式：时间戳+文件名。保证了文件名不重复
                FileUtil.writeBytes(file.getBytes(), filePath + flag + "." + fileName);
                System.out.println(fileName + "上传成功");
                Thread.sleep(1L);
            } catch (Exception e) {
                System.out.println(fileName + "文件上传失败");
            }
            return Result.success(flag);
        }
    }

    /*
       获取文件
        */
    @GetMapping("/{flag}")
    public void avatarPath(@PathVariable String flag, HttpServletRequest request, HttpServletResponse response) {

        if (!FileUtil.isDirectory(filePath)) {
            FileUtil.mkdir(filePath);
        }

        List<String> filesNames = FileUtil.listFileNames(filePath);
        //获取文件名
        String videoFile = filesNames.stream().filter(name -> name.contains(flag)).findAny().orElse("");
        if (StrUtil.isEmpty(videoFile)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            return Result.error("文件不存在");
        }
        File file = new File(filePath + videoFile);
        long fileLength = file.length();

        String range = request.getHeader("Range");

        response.setHeader("Accept-Ranges", "bytes");
        response.setContentType("video/mp4");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             OutputStream os = response.getOutputStream()) {

            if (range != null) {
                // 处理Range请求头
                String[] parts = range.replace("bytes=", "").split("-");
                long start = Long.parseLong(parts[0]);
                long end = parts.length > 1 && !parts[1].isEmpty() ? Long.parseLong(parts[1]) : fileLength - 1;
                long contentLength = end - start + 1;

                // 设置206部分内容返回
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileLength));
                response.setHeader("Content-Length", String.valueOf(contentLength));

                raf.seek(start);
                byte[] buffer = new byte[1024 * 8];
                long bytesRemaining = contentLength;
                int len;
                while (bytesRemaining > 0 && (len = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesRemaining))) != -1) {
                    os.write(buffer, 0, len);
                    bytesRemaining -= len;
                }
            } else {
                // 没有 Range 请求，返回完整视频
                response.setHeader("Content-Length", String.valueOf(fileLength));
                byte[] buffer = new byte[1024 * 8];
                int len;
                while ((len = raf.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }

            os.flush();
//            return Result.success("视频流处理成功");
        } catch (IOException e) {
            System.out.println("视频流拉取失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            return Result.error("视频流拉取失败");
        }

    }

    @PostMapping("/commit/{flag}")//vue点击提交 给处理模块，返回 done:{flag}
    public Result commit(@PathVariable String flag) {
        synchronized (FileController.class) {
            List<String> filesNames = FileUtil.listFileNames(filePath);
            //获取文件名
            String videoFile = filesNames.stream().filter(name -> name.contains(flag)).findAny().orElse("");
            if (StrUtil.isEmpty(videoFile)) {
                return Result.error("文件未找到");
            }

            try {
                FileSystemResource resource = new FileSystemResource(new File(filePath + videoFile));

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentLength(resource.contentLength());

                HttpEntity<Resource> entity = new HttpEntity<>(resource, headers);

                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.postForObject("http://localhost:8083/receive?flag=" + flag, entity, String.class);
                System.out.println("处理模块返回：" + response);
                // 注册轮询
                pollingService.register(flag);
                return Result.success(response); // response 可以是文件名或结果信息
            } catch (Exception e) {
                System.out.println(videoFile + "提交给搜索模块失败");
            }
            return Result.success(flag);
        }
    }

    @GetMapping("/status/{flag}")//前端获知处理状态
    public Result getProcessingStatus(@PathVariable String flag) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String status = restTemplate.getForObject(
                    "http://localhost:8083/status/" + flag,
                    String.class
            );
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取处理状态失败");
        }
    }

    @GetMapping("/result/{flag}")// 下载处理后的视频（后端从 C++ 拉流）
    public void getResult(@PathVariable String flag, HttpServletResponse response) {
        try {
            RestTemplate rest = new RestTemplate();
            ResponseEntity<byte[]> entity = rest.getForEntity("http://localhost:8083/result/" + flag, byte[].class);
            byte[] body = entity.getBody();

            response.setContentType("video/mp4");
            response.setHeader("Content-Disposition", "attachment; filename=result.mp4");
            response.getOutputStream().write(body);
            response.flushBuffer();
        } catch (Exception e) {
            System.out.println("获取处理后视频失败：" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
