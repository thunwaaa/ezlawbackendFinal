package com.example.ezlawbackend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){
        String path = request.getRequestURI();
        if (path.contains("/api/auth/login") ||
                path.contains("/api/auth/signup") ||
                path.contains("/api/webhook/stripe") ||
                path.contains("/api/laws/**")) {
            logger.info("Bypassing authentication for path: {}", path);
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            logger.warn("Unauthorized access attempt to path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }

}