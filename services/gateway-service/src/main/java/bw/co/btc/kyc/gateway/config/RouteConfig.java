package bw.co.btc.kyc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Value("${routes.onboarding:http://onboarding-service:8080}")
    private String onboardingUri;

    @Value("${routes.document:http://document-service:8080}")
    private String documentUri;

    @Value("${routes.biometrics:http://biometrics-service:8080}")
    private String biometricsUri;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder b) {
        return b.routes()
                .route("admin-tenants", r -> r.path("/admin/v1/tenants/**").uri(onboardingUri))
                .route("public-onboarding", r -> r.path("/public/v1/sessions/**")
                        .and().predicate(p -> !p.getRequest().getURI().getPath().contains("/documents"))
                        .uri(onboardingUri))
                .route("public-documents", r -> r.path("/public/v1/sessions/**/documents").uri(documentUri))
                .route("public-biometrics", r -> r.path("/public/v1/sessions/**/biometrics/**").uri(biometricsUri))
                .build();
    }
}
