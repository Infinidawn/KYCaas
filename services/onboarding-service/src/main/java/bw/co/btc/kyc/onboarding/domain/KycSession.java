package bw.co.btc.kyc.onboarding.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="kyc_session")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KycSession {
  @Id private UUID id;
  private UUID tenant_id;
  private String channel;
  private String phone;
  private String state;
  private Instant created_at;
  private Instant decided_at;
}
