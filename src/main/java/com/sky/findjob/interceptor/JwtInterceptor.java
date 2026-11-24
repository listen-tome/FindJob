package com.sky.findjob.interceptor;

import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.utils.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BusinessException("未登录，请先登录", HttpStatus.UNAUTHORIZED);
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            UserContext.set(jwtUtil.getUserId(token));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException("登录已过期，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}