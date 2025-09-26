package bw.co.btc.kyc.risk.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="risk_decision")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decision {
  @Id private UUID session_id;
  private UUID tenant_id;
  private String status;  // PENDING | AUTO_APPROVED | AUTO_REJECTED | REVIEW
  @Column(length=2000) private String reasons;
  private Instant created_at;
}
