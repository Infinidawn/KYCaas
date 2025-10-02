package bw.co.btc.kyc.onboarding.config;

import bw.co.btc.kyc.onboarding.entity.Tenant;
import bw.co.btc.kyc.onboarding.security.TenantContext;
import bw.co.btc.kyc.onboarding.service.TenantService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Request-time tenant resolution & enforcement.
 *
 * Contract:
 *  - Requires header: X-API-Key: "<keyId>.<secret>"
 *  - On success: TenantContext.set(tenantId, tenantName)
 *  - On failure: 401 JSON { "error": "..."}
 *
 * Notes:
 *  - Admin endpoints (/admin/v1/tenants/**) are left unguarded HERE;
 *    protect them behind gateway/IAM.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final TenantService tenantService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getRequestURI();
        if (path.startsWith("/actuator")) return true;
        if (path.startsWith("/admin/v1/tenants")) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String apiKey = header(req, "X-API-Key");
            if (apiKey == null || apiKey.isBlank()) {
                unauthorized(res, "Missing X-API-Key");
                return;
            }

            var tenantOpt = tenantService.validateApiKey(apiKey.trim());
            if (tenantOpt.isEmpty()) {
                unauthorized(res, "Invalid or revoked API key");
                return;
            }

            applyTenant(tenantOpt.get());
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }

    // ---------------- helpers ----------------

    private static String header(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        return (v == null || v.isBlank()) ? null : v;
    }

    private static void applyTenant(Tenant tenant) {
        TenantContext.set(tenant.getId(), tenant.getName());
    }

    private static void unauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json");
        byte[] body = ("{\"error\":\"" + escape(message) + "\"}").getBytes(StandardCharsets.UTF_8);
        res.getOutputStream().write(body);
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }
}


