package bw.co.btc.kyc.onboarding.entity;

import bw.co.btc.kyc.onboarding.enumeration.KycDecisionStatus;
import com.google.errorprone.annotations.Immutable;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent KYC decision for a session, written by the risk/decision engine.
 * This is read-only from the onboarding service perspective.
 */
@Entity
@Table(name = "kyc_decision", schema = "risk_engine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
public class KycDecision {
    @Id
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private KycDecisionStatus status;

    @Column(name = "reasons")
    private String reasons;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
