package bw.co.btc.kyc.risk.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Aggregated decision per KYC session.
 * - Holds OCR + Biometric slices
 * - Stores final/overall status for the session
 */
@Entity
@Table(
        name = "kyc_decision",
        schema = "risk_engine",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_kyc_decision_session",
                columnNames = {"session_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycDecision {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid default gen_random_uuid()")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    // ---------- OCR slice ----------
    @Column(name = "ocr_status", length = 16)
    private String ocrStatus;     // PASS | FAIL | REVIEW | null

    @Column(name = "ocr_score")
    private Double ocrScore;

    // ---------- Biometric slice ----------
    @Column(name = "biometric_status", length = 16)
    private String biometricStatus;  // future use

    @Column(name = "biometric_score")
    private Double biometricScore;


    // ---------- Aggregated ----------
    @Column(name = "overall_status", length = 16)
    private String overallStatus;

    @Column(name = "reasons_json", columnDefinition = "text")
    private String reasonsJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        updatedAt = Instant.now();
    }
}
