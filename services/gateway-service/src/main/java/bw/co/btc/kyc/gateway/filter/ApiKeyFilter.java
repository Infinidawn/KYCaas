package bw.co.btc.kyc.gateway.filter;

import bw.co.btc.kyc.gateway.client.TenantClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private final TenantClient tenantClient;

    @Value("${security.apiKey.header:X-API-Key}")
    private String apiKeyHeader;

    @Value("${security.injectHeaders:true}")
    private boolean injectHeaders;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var path = request.getURI().getPath();

        if (path.startsWith("/actuator") || path.startsWith("/admin/")) {
            return chain.filter(exchange);
        }

        String apiKey = request.getHeaders().getFirst(apiKeyHeader);
        if (apiKey == null || apiKey.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        TenantClient.ResolveResponse resolved;
        try {
            resolved = tenantClient.resolve(apiKey.trim());
        } catch (Exception e) {
            log.error("[Gateway] API key validation failed via onboarding: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (resolved == null || resolved.tenantId() == null || resolved.tenantId().isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (injectHeaders) {
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .headers(h -> {
                        h.remove(apiKeyHeader);
                        h.set("X-Tenant-Id", resolved.tenantId());
                        h.set("X-Tenant-Name", resolved.tenantName() == null ? "" : resolved.tenantName());
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
