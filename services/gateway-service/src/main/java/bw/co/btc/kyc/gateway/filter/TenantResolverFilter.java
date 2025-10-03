package bw.co.btc.kyc.gateway.filter;

import bw.co.btc.kyc.gateway.client.TenantClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class TenantResolverFilter extends AbstractGatewayFilterFactory<TenantResolverFilter.Config> {


    private final TenantClient tenantClient;


    @Data
    public static class Config {
        private String apiKeyHeader = "X-API-KEY"; // must align with ApiKeyFilter
        private String tenantHeader = "X-Tenant-Id";
        private boolean required = true; // if true, 401 when tenant cannot be resolved
    }


    public TenantResolverFilter(TenantClient tenantClient) { super(Config.class);
        this.tenantClient = tenantClient;
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst(config.getApiKeyHeader());
            return tenantClient.resolveTenantId(apiKey)
                    .flatMap(tenantId -> {
                        ServerHttpRequest mutated = exchange.getRequest().mutate()
                                .header(config.getTenantHeader(), tenantId)
                                .build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        if (config.isRequired()) {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    }));
        };
    }
}
