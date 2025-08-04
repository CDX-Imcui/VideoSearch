package com.example.springdemo.service;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
@Slf4j
@Service
@EnableScheduling
public class PollingService {

    private final RestTemplate restTemplate = new RestTemplate();
    // 使用 Spring TaskScheduler 来动态调度任务
    private final TaskScheduler scheduler = new ConcurrentTaskScheduler();
    // 存储每个 guid 对应的 ScheduledFuture，用于取消
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    // 从配置文件读取 Crow 服务地址和任务标识
    @Value("${crow.base-url:http://localhost:8083}")
    private String crowBaseUrl;

    /**
     * 动态注册一个 guid 的轮询任务
     */
    public void register(String guid) {
        // 如果已经存在，则不重复创建
        if (tasks.containsKey(guid)) {
            log.warn("Task for guid={} already exists", guid);
            return;
        }

        Runnable task = () -> {
            try {
                String statusUrl = crowBaseUrl + "/task_status?flag=" + guid;
                String body = restTemplate.getForObject(statusUrl, String.class);
                JSONObject json = new JSONObject(body);
                String status = json.optString("status", "");
                log.info("[{}] status = {}", guid, status);

                if ("finished".equalsIgnoreCase(status)) {
                    downloadVideo(guid);
                    cancel(guid);//下载完成后，取消对应的轮询任务，确保任务不会继续运行
                }
            } catch (Exception ex) {
                log.error("[{}] polling error: {}", guid, ex.getMessage());
            }
        };

        ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(task, new Date(), TimeUnit.SECONDS.toMillis(5));
        tasks.put(guid, future);
        log.info("Registered polling task for guid={}", guid);
    }

    /** 取消并移除轮询任务 */
    public void cancel(String guid) {
        ScheduledFuture<?> future = tasks.remove(guid);
        if (future != null) {
            future.cancel(false);
            log.info("Cancelled polling task for guid={}", guid);
        }
    }

    /** 下载处理后的视频 */
    private void downloadVideo(String guid) {
        String url = crowBaseUrl + "/result?flag=" + guid;
        try {
            ResponseEntity<ByteArrayResource> resp = restTemplate.exchange(
                    RequestEntity.get(URI.create(url))
                            .header("Accept", "application/octet-stream")
                            .build(),
                    ByteArrayResource.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                byte[] data = resp.getBody().getByteArray();
                try (FileOutputStream fos = new FileOutputStream("processed_" + guid + ".mp4")) {
                    fos.write(data);
                    log.info("Video for {} downloaded.", guid);
                }
            } else {
                log.error("Download failed for {}: HTTP {}", guid, resp.getStatusCodeValue());
            }
        } catch (IOException | RuntimeException e) {
            log.error("Exception downloading video for {}: {}", guid, e.getMessage(), e);
        }
    }

    /** 容器关闭时，取消所有任务 */
    @PreDestroy
    public void shutdown() {
        tasks.keySet().forEach(this::cancel);
    }
}