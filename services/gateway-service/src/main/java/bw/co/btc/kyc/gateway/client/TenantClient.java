package bw.co.btc.kyc.gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantClient {

    private final CacheManager cacheManager;

    @Qualifier("tenantWebClient")
    private final WebClient tenantWebClient;

    // Prefer dot-case properties
    @Value("${tenant.resolve.path:/internal/tenants/resolve}")
    private String resolvePath;

    // Fallback if someone still sets a full URL by mistake
    @Value("${tenant.resolve.full:#{null}}")
    private String resolveFull; // e.g. http://onboarding-service:8080/internal/tenants/resolve

    private record TenantResolveResponse(String tenantId) {}

    public Mono<String> resolveTenantId(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) return Mono.empty();

        String cached = cache().get(apiKey, String.class);
        if (cached != null) return Mono.just(cached);

        Mono<TenantResolveResponse> call =
                (resolveFull != null && resolveFull.startsWith("http"))
                        // Treat as FULL URL (avoids single-slash issues entirely)
                        ? tenantWebClient.get().uri(resolveFull + "?apiKey={apiKey}", apiKey).retrieve()
                        .bodyToMono(TenantResolveResponse.class)
                        // Use baseUrl + relative path
                        : tenantWebClient.get()
                        .uri(b -> b.path(safePath(resolvePath)).queryParam("apiKey", apiKey).build())
                        .retrieve().bodyToMono(TenantResolveResponse.class);

        log.debug("[Gateway] Tenant resolve using {} (path='{}', full='{}')",
                tenantWebClient, resolvePath, resolveFull);

        return call
                .map(TenantResolveResponse::tenantId)
                .doOnNext(tenantId -> {
                    cache().put(apiKey, tenantId);
                    log.debug("[Gateway] Tenant resolved apiKey={} → {}", mask(apiKey), tenantId);
                })
                .onErrorResume(ex -> {
                    log.warn("[Gateway] Tenant resolve failed: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    private String safePath(String p) { return (p != null && p.startsWith("/")) ? p : "/" + (p == null ? "" : p); }

    private Cache cache() {
        Cache c = cacheManager.getCache("tenant-resolve"); // or "tenantByApiKey" – see Step 3
        if (c == null) throw new IllegalStateException("Missing cache 'tenant-resolve'");
        return c;
    }

    private String mask(String apiKey) {
        if (apiKey.length() < 8) return "****";
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
