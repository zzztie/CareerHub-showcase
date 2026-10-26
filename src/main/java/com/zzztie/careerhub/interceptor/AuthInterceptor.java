package com.zzztie.careerhub.interceptor;

import com.zzztie.careerhub.context.UserContext;
import com.zzztie.careerhub.exception.InvalidTokenException;
import com.zzztie.careerhub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 在受保护接口执行前校验 JWT，并把用户 ID 放入当前请求线程的上下文。
 * 请求结束后必须清理 ThreadLocal，避免线程复用时串用上一位用户的身份。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;

    public AuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        String token = authorization.substring(7);

        Long userId = jwtService.parseUserId(token);
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        // 无论控制器成功还是抛出异常，都清理当前线程的用户身份。
        UserContext.clear();
    }
}
