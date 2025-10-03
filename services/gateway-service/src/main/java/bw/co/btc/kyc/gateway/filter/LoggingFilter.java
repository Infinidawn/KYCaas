package bw.co.btc.kyc.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


import java.util.UUID;


@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {


    @Data
    public static class Config { }


    public LoggingFilter() { super(Config.class); }


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String headerCorrId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
            final String corrId = (headerCorrId != null ? headerCorrId : UUID.randomUUID().toString());

            ServerHttpRequest req = exchange.getRequest().mutate()
                    .header("X-Correlation-Id", corrId)
                    .build();

            log.info("➡️  {} {} corrId={}", req.getMethod(), req.getURI(), corrId);
            return chain.filter(exchange.mutate().request(req).build())
                    .then(Mono.fromRunnable(() ->
                            log.info("⬅️  {} {} corrId={}", req.getMethod(), req.getURI(), corrId)));
        };
    }
}
