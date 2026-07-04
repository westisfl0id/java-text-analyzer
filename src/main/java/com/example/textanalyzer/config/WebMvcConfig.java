package com.example.textanalyzer.config;

import com.example.textanalyzer.rest.audit.AuditInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers MVC infrastructure components used by the REST API.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuditInterceptor auditInterceptor;

    public WebMvcConfig(@NonNull AuditInterceptor auditInterceptor) {
        this.auditInterceptor = auditInterceptor;
    }

    /**
     * Adds cross-cutting interceptors to the Spring MVC request pipeline.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(auditInterceptor)
                .addPathPatterns("/api/analyze");
    }
}