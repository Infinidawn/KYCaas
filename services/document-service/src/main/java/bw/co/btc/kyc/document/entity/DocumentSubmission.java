package bw.co.btc.kyc.document.entity;

import bw.co.btc.kyc.document.enumeration.IdType;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Raw submission metadata captured when client calls POST /sessions/{id}/documents.
 * This is BEFORE OCR and mirrors what the user/app provided.
 */
@Entity
@Table(name = "document_submission", schema = "document")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentSubmission {

    @Id private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private IdType idType;   // OMANG | PASSPORT

    /** User-provided ID number (may differ from OCR-extracted). */
    @Column(name = "provided_id_number")           private String providedIdNumber;

    /** Storage addressing and convenience URLs for audits. */
    @Column(name = "bucket")
    private String bucket;
    @Column(name = "front_object_key")
    private String frontObjectKey;
    @Column(name = "back_object_key")
    private String backObjectKey;
    @Column(name = "front_image_url", length = 4000)
    private String frontImageUrl;
    @Column(name = "back_image_url",  length = 4000)
    private String backImageUrl;

    @Column(name = "received_at", nullable = false) private Instant receivedAt;

    @PrePersist void pre() {
        if (id == null) id = UUID.randomUUID();
        if (receivedAt == null) receivedAt = Instant.now();
    }
}