package bw.co.btc.kyc.onboarding.api;

import bw.co.btc.kyc.onboarding.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin-only API for tenant lifecycle management.
 *
 * WARNING:
 *  - Expose only internally or behind IAM/Gateway.
 *  - Normal clients use X-API-Key header, never these endpoints.
 */
@RestController
@RequestMapping("/admin/v1/tenants")
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantService tenantService;

    // ---------- Create tenant with initial API key ----------
    @PostMapping
    public ResponseEntity<TenantService.TenantCreated> create(@Valid @RequestBody CreateTenantRequest req) {
        var created = tenantService.createTenant(req.getName(), req.getAdminEmail(), req.getContactPhone());
        return ResponseEntity.ok(created);
    }

    // ---------- Rotate tenant API key ----------
    @PostMapping("/{id}/rotate-key")
    public ResponseEntity<TenantService.TenantRotated> rotateKey(@PathVariable UUID id) {
        var rotated = tenantService.rotateApiKey(id);
        return ResponseEntity.ok(rotated);
    }

    // ---------- List tenants ----------
    @GetMapping
    public List<TenantService.TenantView> list() {
        return tenantService.list();
    }

    // ---------- Get single tenant ----------
    @GetMapping("/{id}")
    public ResponseEntity<TenantService.TenantView> get(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.get(id));
    }

    // ---------- Change tenant status ----------
    @PatchMapping("/{id}/status")
    public ResponseEntity<TenantService.TenantView> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest req) {
        var updated = tenantService.changeStatus(id, req.getStatus());
        return ResponseEntity.ok(updated);
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
