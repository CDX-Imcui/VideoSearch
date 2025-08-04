package com.example.springdemo.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.springdemo.entity.Admin;
import com.example.springdemo.exception.CustomException;
import com.example.springdemo.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * JWT拦截器，用于验证token
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);

    @Resource
    private AdminService adminService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        /**
         跨域资源共享（CORS）策略要求浏览器在发出实际请求之前，先发出预检请求（OPTIONS 请求），以确定服务器是否允许该跨域请求。
         但OPTIONS请求不包含 token，因此当后端服务严格要求所有请求都携带 token，预检请求会被拒绝，那么实际请求也会被浏览器阻止。
         为了解决问题，在 JwtInterceptor 中添加一段代码来允许 OPTIONS 请求通过，而不进行 token 验证。
         */
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            return true;
        }

        //从请求头或请求参数中获取token
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        //开始认证
        if (StrUtil.isBlank(token)) {
            log.error("token为空，请重新登录");
            throw new CustomException("token为空，请重新登录");

        }
        String adminID;
        Admin admin;
        try {
            adminID = JWT.decode(token).getAudience().get(0);
            //根据token中的ID查询数据库用户信息
            admin = adminService.getAdminById(Integer.parseInt(adminID));
        } catch (Exception e) {
            String errMsg = "token验证失败，请重新登录";
            log.error(errMsg + "，token=" + token, e);
            throw new CustomException(errMsg);
        }
        if (admin == null) {
            log.error("用户不存在");
            throw new CustomException("用户不存在，请重新登录");
        }

        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(admin.getPassword())).build();
            jwtVerifier.verify(token);//验证token是否正确
        } catch (JWTVerificationException e) {
            throw new CustomException("token验证失败，请重新登录");
        }
        log.info("token验证通过，用户ID：{}", adminID);
        return true;
    }
}
