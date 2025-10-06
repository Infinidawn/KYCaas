package bw.co.btc.kyc.gateway.filter;

import bw.co.btc.kyc.gateway.client.TenantClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ApiKeyFilter extends AbstractGatewayFilterFactory<ApiKeyFilter.Config> {
    private static final String ATTR_AUTH_DONE = "ApiKeyFilter.authDone";

    private final TenantClient tenantClient;

    @Data
    public static class Config {
        private List<String> whitelist = new ArrayList<>();
        private String headerName = "X-API-Key";   // unified header name
        private String tenantHeader = "X-Tenant-Id";
        private boolean required = true;           // if true: missing/invalid key -> 401
    }

    public ApiKeyFilter(TenantClient tenantClient) { super(Config.class);
        this.tenantClient = tenantClient;
    }

    @Override
    public GatewayFilter apply(Config cfg) {

        log.debug("[ApiKeyFilter] instance={} path={}", System.identityHashCode(this), cfg);
        return (exchange, chain) -> {
            // Idempotency guard #1: if we already processed this request, skip
            if (Boolean.TRUE.equals(exchange.getAttribute(ATTR_AUTH_DONE))) {
                log.debug("[ApiKeyFilter] Already processed; skipping");
                return chain.filter(exchange);
            }

            final var req = exchange.getRequest();
            final var path = req.getURI().getPath();

            // If tenant already present (e.g., upstream injected), mark done and continue
            String existingTenant = req.getHeaders().getFirst(cfg.getTenantHeader());
            if (existingTenant != null && !existingTenant.isBlank()) {
                log.debug("[ApiKeyFilter] {} already present ({}); skipping resolve",
                        cfg.getTenantHeader(), existingTenant);
                exchange.getAttributes().put(ATTR_AUTH_DONE, true);
                return chain.filter(exchange);
            }

            // Whitelist: allow some paths without API key
            if (cfg.whitelist != null && !cfg.whitelist.isEmpty()) {
                var pm = new org.springframework.util.AntPathMatcher();
                for (String pattern : cfg.whitelist) {
                    if (pm.match(pattern, path)) {
                        return chain.filter(exchange);
                    }
                }
            }

            // Require API key?
            String apiKey = req.getHeaders().getFirst(cfg.getHeaderName());
            if (apiKey == null || apiKey.isBlank()) {
                if (cfg.isRequired()) {
                    log.warn("[ApiKeyFilter] Rejecting {} {} reason=missing {}",
                            req.getMethod(), req.getURI(), cfg.getHeaderName());
                    return unauthorizedJson(exchange.getResponse(), "Missing API key");
                }
                return chain.filter(exchange);
            }

            apiKey = apiKey.trim();

            // Resolve tenant only once; on success inject header and mark as done
            return tenantClient.resolveTenantId(apiKey)
                    .flatMap(tenantId -> {
                        ServerHttpRequest mutatedReq = req.mutate()
                                .header(cfg.getTenantHeader(), tenantId)
                                .build();
                        var mutatedEx = exchange.mutate().request(mutatedReq).build();
                        mutatedEx.getAttributes().put(ATTR_AUTH_DONE, true); // mark on mutated exchange
                        log.debug("[ApiKeyFilter] Resolved tenantId={} and injected header {}",
                                tenantId, cfg.getTenantHeader());
                        return chain.filter(mutatedEx);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        // We had a key but couldn't resolve → invalid/revoked
                        if (cfg.isRequired()) {
                            log.warn("[ApiKeyFilter] Invalid or revoked API key → 401");
                            return unauthorizedJson(exchange.getResponse(), "Invalid or expired API key");
                        }
                        return chain.filter(exchange);
                    }));
        };
    }

    private Mono<Void> unauthorizedJson(ServerHttpResponse res, String message) {
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"" + (message == null ? "" : message.replace("\"", "\\\"")) + "\"}";
        DataBuffer buf = res.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return res.writeWith(Mono.just(buf));
    }
}
