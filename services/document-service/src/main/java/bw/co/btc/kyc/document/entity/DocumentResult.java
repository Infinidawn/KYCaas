package bw.co.btc.kyc.document.entity;

import bw.co.btc.kyc.document.enumeration.IdType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * OCR/verification outcome for an uploaded document.
 * Contains extracted identity fields + scan quality/MRZ signals.
 */
@Entity
@Table(name = "document_result", schema = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResult {

  @Id private UUID id;

  @Column(name = "tenant_id")
  private UUID tenantId;
  @Column(name = "session_id", nullable = false)
  private UUID sessionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "id_type")
  private IdType idType;   // OMANG | PASSPORT

  /** OCR-extracted ID number (may differ from user-provided). */
  @Column(name = "ocr_id_number")
  private String ocrIdNumber;

  // --- Extracted person/document attributes (Botswana-realistic) ---
  @Column(name = "surname")
  private String surname;
  @Column(name = "given_names")
  private String givenNames;     // multiple names space-separated
  @Column(name = "full_name")
  private String fullName;       // optional: convenience field
  @Column(name = "date_of_birth")
  private String dateOfBirth;    // ISO "YYYY-MM-DD"
  @Column(name = "sex")
  private String sex;            // "M" | "F"
  @Column(name = "nationality")
  private String nationality;    // "BWA" | "BW" etc.
  @Column(name = "document_number")
  private String documentNumber; // passport number, etc.
  @Column(name = "date_of_issue")
  private String dateOfIssue;    // ISO
  @Column(name = "date_of_expiry")
  private String dateOfExpiry;   // ISO

  // --- MRZ / quality / status ---
  @Column(name = "mrz_present")
  private Boolean mrzPresent;
  @Column(name = "mrz_valid")
  private Boolean mrzValid;
  @Column(name = "mrz_line1")
  private String mrzLine1;
  @Column(name = "mrz_line2")
  private String mrzLine2;

  @Column(name = "quality_score")
  private Double qualityScore;   // 0..1
  @Column(name = "status")
  private String status;         // PASS | FAIL | REVIEW
  @Column(name = "reasons")
  private String reasons;        // comma-separated reason codes

  // --- Original image locations (for audits) ---
  @Column(name = "front_image_url", length = 4000)
  private String frontImageUrl;
  @Column(name = "back_image_url",  length = 4000)
  private String backImageUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @PrePersist void pre() {
    if (id == null) id = UUID.randomUUID();
    if (createdAt == null) createdAt = Instant.now();
  }
}