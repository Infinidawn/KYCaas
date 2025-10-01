package bw.co.btc.kyc.risk.entity;

import bw.co.btc.kyc.risk.enumeration.SignalSource;
import bw.co.btc.kyc.risk.enumeration.SignalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Latest signal per (sessionId, source).
 * Example sources: DOCUMENT (OCR result), BIOMETRIC (selfie/liveness), etc.
 * Holds summarized status + raw payload for audits.
 */
@Entity
@Table(
        name = "verification_signal",
        schema = "risk_engine",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_verification_signal_session_source",
                columnNames = {"session_id", "source"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationSignal {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid default gen_random_uuid()")
    private UUID id;

    @Column(name = "session_id", nullable = false, columnDefinition = "uuid")
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 24)
    private SignalSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SignalStatus status; // PASS | FAIL | REVIEW

    @Column(name = "score")
    private Double score;

    /** JSON payload with whatever the source emits (MRZ lines, reasons, etc.). */
    @Column(name = "payload_json", columnDefinition = "text")
    private String payloadJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    public void ts() {
        updatedAt = Instant.now();
    }
}
