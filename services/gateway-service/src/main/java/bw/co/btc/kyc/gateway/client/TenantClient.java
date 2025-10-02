package bw.co.btc.kyc.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tenantClient", url = "${clients.onboarding:http://onboarding-service:8080}")
public interface TenantClient {

    @GetMapping("/internal/tenants/resolve")
    ResolveResponse resolve(@RequestParam("apiKey") String apiKey);

    record ResolveResponse(String tenantId, String tenantName) {}
}
