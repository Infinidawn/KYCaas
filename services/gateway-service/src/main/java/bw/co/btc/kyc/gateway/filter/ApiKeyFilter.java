package bw.co.btc.kyc.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class ApiKeyFilter extends AbstractGatewayFilterFactory<ApiKeyFilter.Config> {


    private final AntPathMatcher matcher = new AntPathMatcher();


    @Data
    public static class Config {
        private List<String> whitelist = new ArrayList<>();
        private String headerName = "X-API-KEY"; // configurable if needed
    }


    public ApiKeyFilter() {
        super(Config.class);
    }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();


// Allow whitelisted paths through without an API key
            for (String pattern : config.getWhitelist()) {
                if (matcher.match(pattern, path)) {
                    return chain.filter(exchange);
                }
            }


            String apiKey = exchange.getRequest().getHeaders().getFirst(config.getHeaderName());
            if (apiKey == null || apiKey.isBlank()) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }


// Optionally normalize header
            ServerHttpRequest req = exchange.getRequest().mutate()
                    .header(config.getHeaderName(), apiKey.trim())
                    .build();


            return chain.filter(exchange.mutate().request(req).build());
        };
    }
}
