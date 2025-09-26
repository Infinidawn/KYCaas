package bw.co.btc.kyc.document.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "document_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResult {
  @Id private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId;

  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "id_type")
  private String idType;

  @Column(name = "id_number")
  private String idNumber;

  @Column(name = "front_image_url", length = 4000)
  private String frontImageUrl;

  private Instant createdAt;
}

