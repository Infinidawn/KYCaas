package bw.co.btc.kyc.document.config;

import bw.co.btc.kyc.document.security.TenantContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class TenantHeaderFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // allow health or actuator without tenant header
        String p = request.getRequestURI();
        return p.startsWith("/actuator") || p.equals("/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String header = req.getHeader("X-Tenant-Id");
            if (header == null || header.isBlank()) {
                unauthorized(res, "Missing X-Tenant-Id");
                return;
            }
            UUID tenantId;
            try {
                tenantId = UUID.fromString(header.trim());
            } catch (IllegalArgumentException e) {
                unauthorized(res, "Invalid X-Tenant-Id");
                return;
            }
            TenantContext.set(tenantId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    private static void unauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json");
        byte[] body = ("{\"error\":\"" + message.replace("\"","\\\"") + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        res.getOutputStream().write(body);
    }
}
