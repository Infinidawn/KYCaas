package bw.co.btc.kyc.biometrics.domain;
import jakarta.persistence.*; import lombok.*; import java.time.Instant; import java.util.UUID;
@Entity
@Table(name = "biometric_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiometricResult {
  @Id private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId;

  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "selfie_image_url", length = 4000)
  private String selfieImageUrl;

  private Instant createdAt;
}
