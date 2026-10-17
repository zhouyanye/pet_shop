package com.yanleaf.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil; //校验token
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        // 跨域预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String path = request.getRequestURI();
        String token = extractToken(request);

        // 只拦 /api/admin/** 管理员后台的接口
        if (path.startsWith("/api/admin/")) {

            // 登录接口放行
            if (path.equals("/api/admin/login")) return true;
            // token 为空或校验失败 → 返回 401
            if (token == null || !jwtUtil.validate(token)) {
                writeError(response, 401, "管理员未登录或登录已过期");
                return false;
            }
            // token 有效但不是管理员角色 → 返回 403
            if (!"ADMIN".equals(jwtUtil.getRole(token))) {
                writeError(response, 403, "无权限访问");
                return false;
            }
        }

        return true;
    }

    private String extractToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) return h.substring(7);
        return null;
    }

    private void writeError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(200);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(objectMapper.writeValueAsString(
                Result.fail(code, msg)));
    }
}