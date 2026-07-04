package com.example.textanalyzer.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.io.IOException;

/**
 * Wraps HTTP requests so interceptors can read cached request body.
 */
@Component
public class RequestCachingFilter extends OncePerRequestFilter {

    private static final int CACHE_LIMIT_BYTES = 4096;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (shouldCache(request) && !(request instanceof ContentCachingRequestWrapper)) {
            filterChain.doFilter(new ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES), response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldCache(@NonNull HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals(request.getContextPath() + "/api/analyze");
    }
}