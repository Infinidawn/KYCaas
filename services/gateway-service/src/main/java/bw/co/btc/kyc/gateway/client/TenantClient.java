package bw.co.btc.kyc.gateway.client;

import bw.co.btc.kyc.gateway.model.TenantResolveResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class TenantClient {


    private final CacheManager cacheManager;


    @Value("${TENANT_RESOLVE_URL:http://onboarding:8080/internal/tenants/resolve}")
    private String resolveUrl;


    private Cache cache() { return cacheManager.getCache("tenantByApiKey"); }


    public Mono<String> resolveTenantId(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) return Mono.empty();


        String cached = cache().get(apiKey, String.class);
        if (cached != null) return Mono.just(cached);


        return WebClient.create()
                .get()
                .uri(uriBuilder -> uriBuilder.path(resolveUrl).queryParam("apiKey", apiKey).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TenantResolveResponse.class)
                .map(TenantResolveResponse::tenantId)
                .doOnNext(t -> cache().put(apiKey, t))
                .doOnError(err -> log.warn("Tenant resolve failed: {}", err.getMessage()));
    }
}
