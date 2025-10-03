package bw.co.btc.kyc.onboarding.api;

import bw.co.btc.kyc.onboarding.entity.Tenant;
import bw.co.btc.kyc.onboarding.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin-only API for tenant lifecycle management.
 *
 * WARNING:
 *  - Expose only internally or behind IAM/Gateway.
 *  - Normal clients use X-API-Key header, never these endpoints.
 */
@RestController
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantService tenantService;

    // ---------- Create tenant with initial API key ----------
    @PostMapping("/admin/v1/tenants")
    public ResponseEntity<TenantService.TenantCreated> create(@Valid @RequestBody CreateTenantRequest req) {
        var created = tenantService.createTenant(req.getName(), req.getAdminEmail(), req.getContactPhone());
        return ResponseEntity.ok(created);
    }

    // ---------- Rotate tenant API key ----------
    @PostMapping("/admin/v1/tenants/{id}/rotate-key")
    public ResponseEntity<TenantService.TenantRotated> rotateKey(@PathVariable UUID id) {
        var rotated = tenantService.rotateApiKey(id);
        return ResponseEntity.ok(rotated);
    }

    // ---------- List tenants ----------
    @GetMapping("/admin/v1/tenants")
    public List<TenantService.TenantView> list() {
        return tenantService.list();
    }

    // ---------- Get single tenant ----------
    @GetMapping("/admin/v1/tenants/{id}")
    public ResponseEntity<TenantService.TenantView> get(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.get(id));
    }

    // ---------- Change tenant status ----------
    @PatchMapping("/admin/v1/tenants/{id}/status")
    public ResponseEntity<TenantService.TenantView> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest req) {
        var updated = tenantService.changeStatus(id, req.getStatus());
        return ResponseEntity.ok(updated);
    }

    /**
     * Resolve tenantId from an API key string.
     * Contract: GET /internal/tenants/resolve?apiKey=<key>
     * Response: 200 { "tenantId": "<uuid>" }  or  401 if invalid.
     */
    @GetMapping("/internal/tenants/resolve")
    public ResponseEntity<Map<String, String>> resolve(@RequestParam String apiKey) {
        return tenantService.validateApiKey(apiKey)
                .map(this::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private ResponseEntity<Map<String, String>> ok(Tenant t) {
        return ResponseEntity.ok(Map.of("tenantId", t.getId().toString()));
    }

    // ---------- DTOs ----------
    @Data
    public static class CreateTenantRequest {
        @NotBlank
        private String name;

        @NotBlank
        private String adminEmail;

        private String contactPhone; // optional
    }

    @Data
    public static class ChangeStatusRequest {
        @NotBlank
        private String status; // ACTIVE | SUSPENDED
    }
}
