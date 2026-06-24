package com.battery.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.battery.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Map<String, Object> TOKEN_STORE = new HashMap<>();

    static {
        // 初始化时与AuthController共享token存储 - 简化方案使用静态变量
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }
        if (token == null || token.isEmpty()) {
            sendError(response, 401, "未登录，请先登录");
            return false;
        }

        // 验证token是否存在（与AuthController的TOKEN_STORE共享）
        if (!AuthTokenStore.isValid(token)) {
            sendError(response, 401, "登录已过期，请重新登录");
            return false;
        }

        return true;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(Result.error(code, message)));
    }
}
