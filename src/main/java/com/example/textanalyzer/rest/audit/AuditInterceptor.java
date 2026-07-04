package com.example.textanalyzer.rest.audit;

import com.example.textanalyzer.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

/**
 * Writes audit entries for successful analysis requests.
 */
@Component
public class AuditInterceptor implements HandlerInterceptor {

    private static final String START_ANALYSIS_PATH = "/api/analyze";

    private final AuditService auditService;

    public AuditInterceptor(@NonNull AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception ex
    ) {
        if (!shouldAudit(request, response, ex)) {
            return;
        }

        auditService.saveEvent(
                resolveUsername(request),
                "START_ANALYSIS",
                buildParameters(request, response)
        );
    }

    private boolean shouldAudit(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @Nullable Exception ex
    ) {
        return ex == null
                && "POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().equals(request.getContextPath() + START_ANALYSIS_PATH)
                && response.getStatus() >= 200
                && response.getStatus() < 300;
    }

    private String resolveUsername(@NonNull HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal == null ? "anonymous" : principal.getName();
    }

    private String buildParameters(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response
    ) {
        String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();

        return "method=" + request.getMethod()
                + ", uri=" + request.getRequestURI() + query
                + ", status=" + response.getStatus()
                + ", remoteAddr=" + request.getRemoteAddr()
                + ", body=" + requestBody(request);
    }

    private String requestBody(@NonNull HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
            return "";
        }

        byte[] content = wrapper.getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }

        return new String(content, StandardCharsets.UTF_8);
    }
}