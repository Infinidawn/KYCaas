package bw.co.btc.kyc.biometrics.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent biometric outcome per session.
 * Stores simulated face-match & liveness check results.
 */
@Entity
@Table(name = "biometric_result", schema = "onboarding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometricResult {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid default gen_random_uuid()")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "selfie_url")
    private String selfieUrl;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "match_status", length = 16)
    private String matchStatus; // MATCHED | MISMATCHED

    @Column(name = "match_score")
    private Double matchScore;

    @Column(name = "liveness_passed")
    private boolean livenessPassed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }
}
