package bw.co.btc.kyc.verification.domain;
import jakarta.persistence.*; import lombok.*; import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant; import java.time.LocalDate; import java.util.UUID;
@Entity @Table(name="verification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Verification {
  @Id @GeneratedValue private UUID id;
  private String channel;
  private String idType; private String idNumber;
  private String firstName; private String lastName; private LocalDate dob;
  private String docFrontUrl; private String selfieUrl;
  private String status; @Column(length=1000) private String reasons;
  @CreationTimestamp private Instant createdAt;
}
