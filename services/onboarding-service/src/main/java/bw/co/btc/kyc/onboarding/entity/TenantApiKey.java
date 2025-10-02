package bw.co.btc.kyc.onboarding.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * API key for authenticating a tenant’s requests.
 *
 * Notes:
 *  - Stored in DB as a UUID keyId + hashed secret (never store cleartext).
 *  - We return cleartext only at creation/rotation time (in service layer).
 *  - Each tenant can have multiple historical keys, but only one ACTIVE at a time.
 */
@Entity
@Table(name = "tenant_api_key", schema = "onboarding", uniqueConstraints = { @UniqueConstraint(name = "uq_tenant_key_id", columnNames = "key_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantApiKey {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid default gen_random_uuid()")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_api_key_tenant"))
    private Tenant tenant;

    /** Stable identifier for this key (shared with client). */
    @Column(name = "key_id", nullable = false, unique = true, columnDefinition = "uuid")
    private UUID keyId;

    /** Secure hash of the secret (e.g., SHA-256 or bcrypt). */
    @Column(name = "secret_hash", nullable = false, length = 128)
    private String secretHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Column(name = "revoked_at")
    private Instant revokedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (keyId == null) keyId = UUID.randomUUID();
    }

    /** Convenience: is this key still valid? */
    public boolean isActive() {
        return revokedAt == null;
    }
}
