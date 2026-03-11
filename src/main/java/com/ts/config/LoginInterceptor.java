package com.ts.config;

import com.ts.TsJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 
 * @author yue.wu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final TsJobProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果未启用登录，直接放行
        if (!Boolean.TRUE.equals(properties.getLogin().getEnabled())) {
            return true;
        }

        // 放行静态资源和登录接口
        String uri = request.getRequestURI();
        if (uri.contains("/ts-job/js/") || 
            uri.contains("/ts-job/css/") || 
            uri.contains("/ts-job/img/") ||
            uri.contains("/ts-job/login") ||
            uri.endsWith(".html") && uri.contains("login")) {
            return true;
        }

        // 检查登录状态
        HttpSession session = request.getSession(false);
        if (session != null && Boolean.TRUE.equals(session.getAttribute("isLoggedIn"))) {
            return true;
        }

        // 未登录，重定向到登录页
        response.sendRedirect("/ts-job/login.html");
        return false;
    }
}
