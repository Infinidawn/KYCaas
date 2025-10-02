package bw.co.btc.kyc.onboarding.service;

import bw.co.btc.kyc.onboarding.entity.Tenant;
import bw.co.btc.kyc.onboarding.entity.TenantApiKey;
import bw.co.btc.kyc.onboarding.repo.TenantApiKeyRepository;
import bw.co.btc.kyc.onboarding.repo.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service handling tenant lifecycle and API key issuance.
 *
 * Responsibilities:
 *  - Create tenant with initial API key
 *  - Rotate API key (revoke old, issue new)
 *  - Lookup tenant (for admin view or request-time filter)
 *  - Change tenant status (ACTIVE / SUSPENDED)
 */
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenants;
    private final TenantApiKeyRepository keys;

    // ---------- DTOs returned for controller ---------
    public record TenantCreated(UUID tenantId, String apiKey) {}
    public record TenantRotated(UUID tenantId, String apiKey) {}
    public record TenantView(UUID id, String name, String status, Instant createdAt) {}

    // ---------- Public API ----------

    @Transactional
    public TenantCreated createTenant(String name, String adminEmail, String contactPhone) {
        tenants.findByNameIgnoreCase(name).ifPresent(t -> {
            throw new IllegalArgumentException("Tenant with name already exists: " + name);
        });

        Tenant t = Tenant.builder()
                .name(name)
                .adminEmail(adminEmail)
                .contactPhone(contactPhone)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();
        tenants.save(t);

        String clearSecret = generateClearSecret();
        String hash = hashSecret(clearSecret);

        TenantApiKey k = TenantApiKey.builder()
                .tenant(t)
                .secretHash(hash)
                .createdAt(Instant.now())
                .build();
        keys.save(k);

        return new TenantCreated(t.getId(), formatApiKey(k.getKeyId(), clearSecret));
    }

    @Transactional
    public TenantRotated rotateApiKey(UUID tenantId) {
        Tenant tenant = tenants.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        // revoke existing keys
        keys.findAll().stream()
                .filter(k -> k.getTenant().equals(tenant) && k.isActive())
                .forEach(k -> {
                    k.setRevokedAt(Instant.now());
                    keys.save(k);
                });

        // new key
        String clearSecret = generateClearSecret();
        String hash = hashSecret(clearSecret);
        TenantApiKey newKey = TenantApiKey.builder()
                .tenant(tenant)
                .secretHash(hash)
                .createdAt(Instant.now())
                .build();
        keys.save(newKey);

        return new TenantRotated(tenant.getId(), formatApiKey(newKey.getKeyId(), clearSecret));
    }

    @Transactional(readOnly = true)
    public TenantView get(UUID tenantId) {
        Tenant t = tenants.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        return new TenantView(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<TenantView> list() {
        return tenants.findAll().stream()
                .map(t -> new TenantView(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt()))
                .toList();
    }

    @Transactional
    public TenantView changeStatus(UUID tenantId, String status) {
        Tenant t = tenants.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));
        if (!status.equals("ACTIVE") && !status.equals("SUSPENDED")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
        t.setStatus(status);
        tenants.save(t);
        return new TenantView(t.getId(), t.getName(), t.getStatus(), t.getCreatedAt());
    }

    // ---------- Helper methods ----------

    /** Generate cleartext secret (random UUID base64). */
    private String generateClearSecret() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    /** Hash secret with SHA-256 (simple placeholder, replace with bcrypt/argon2 in prod). */
    private String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash secret", e);
        }
    }

    /** Combine keyId + clear secret into API key format for clients. */
    private String formatApiKey(UUID keyId, String clearSecret) {
        return keyId + "." + clearSecret;
    }

    // ---------- Request-time validation (used by filters later) ----------

    @Transactional(readOnly = true)
    public Optional<Tenant> validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.contains(".")) return Optional.empty();
        String[] parts = apiKey.split("\\.", 2);
        try {
            UUID keyId = UUID.fromString(parts[0]);
            String secret = parts[1];
            return keys.findByKeyIdAndRevokedAtIsNull(keyId)
                    .filter(k -> k.getSecretHash().equals(hashSecret(secret)))
                    .map(TenantApiKey::getTenant)
                    .filter(t -> "ACTIVE".equalsIgnoreCase(t.getStatus())); // ✅ fixed
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
