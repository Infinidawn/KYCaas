package bw.co.btc.kyc.onboarding.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a subscribing Tenant (customer organization).
 * Each tenant can have one or more API keys for access.
 */
@Entity
@Table(name = "tenant", schema = "onboarding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid default gen_random_uuid()")
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;  // e.g., "XYZ Bank"

    @Column(name = "admin_email", nullable = false, length = 150)
    private String adminEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(nullable = false, length = 20)
    private String status;  // ACTIVE | SUSPENDED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }
}
