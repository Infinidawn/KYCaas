package bw.co.btc.kyc.onboarding.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/** Persistent record of a KYC session. */
@Entity
@Table(name = "kyc_session", schema = "onboarding")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class KycSession {

  @Id
  private UUID id;

  @Column(name = "tenant_id", nullable = false)
  private UUID tenantId;

  @Column(name = "channel", nullable = false)
  private String channel;

  @Column(name = "phone", nullable = false)
  private String phone;

  @Column(name = "state", nullable = false)
  private String state;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
