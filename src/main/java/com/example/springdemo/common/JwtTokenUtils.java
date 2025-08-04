package com.example.springdemo.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springdemo.entity.Admin;
import com.example.springdemo.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Component
public class JwtTokenUtils {
    private static AdminService staticAdminService;
    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtils.class);

    @Resource
    private AdminService adminService;

    @PostConstruct
    public void setAdminService() {
        staticAdminService = adminService;
    }

    /**
     * 生成token
     */
    public static String generateToken(Integer ID, String password) {
        return JWT.create().withAudience(String.valueOf(ID))//ID作为token的唯一标识（载荷）
                .withExpiresAt(DateUtil.offsetHour(new Date(), 72))//小时后token过期
                .sign(Algorithm.HMAC256(password));//使用password作为token的密钥

    }

    /**
     * 获取当前登录用户的信息
     */
    public static Admin getCurrentUser() {
        String token = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");
            if (StrUtil.isBlank(token)) {
                token = request.getParameter("token");
            }
            if (StrUtil.isBlank(token)) {
                log.error("获取当前登录用户信息失败，token:{}", token);
            }
            //解密token，获取用户ID
            String ID = JWT.decode(token).getAudience().get(0);
            return staticAdminService.getAdminById(Integer.valueOf(ID));
        } catch (Exception e) {
            log.error("获取当前登录用户信息失败，token:{}", token, e);
            return null;
        }

    }

}
