package bw.co.btc.kyc.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration
public class WebClientConfig {

    @Bean(name = "tenantWebClient")
    WebClient tenantWebClient(
            @Value("${tenant.resolve.base:http://onboarding-service:8080}") String base) {
        log.info("[Gateway] tenant.resolve.base={}", base);
        return WebClient.builder().baseUrl(base).build();
    }
}