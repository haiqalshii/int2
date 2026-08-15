package com.int2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final String apiKey;

    public ApiKeyFilter(@Value("${app.api-key:}") String apiKey) { this.apiKey = apiKey; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !StringUtils.hasText(apiKey) || request.getRequestURI().equals("/api/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        byte[] expected = apiKey.getBytes(StandardCharsets.UTF_8);
        byte[] supplied = request.getHeader("X-INT2-App-Key") == null ? new byte[0]
                : request.getHeader("X-INT2-App-Key").getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, supplied)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Valid application credentials are required.\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
