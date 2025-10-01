package bw.co.btc.kyc.document.OCR.dto;

import bw.co.btc.kyc.document.enumeration.IdType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private UUID sessionId;
    private UUID tenantId;

    // identity
    private IdType idType;
    private String ocrIdNumber;

    // person/document
    private String surname;
    private String givenNames;
    private String dateOfBirth;  // ISO yyyy-MM-dd
    private String sex;
    private String nationality;
    private String documentNumber;
    private String dateOfIssue;  // ISO yyyy-MM-dd
    private String dateOfExpiry; // ISO yyyy-MM-dd or null

    // MRZ
    private boolean mrzPresent;
    private boolean mrzValid;
    private String mrzLine1;
    private String mrzLine2;

    // quality & verdict
    private Double qualityScore; // 0..1
    private String verdict;      // PASS|FAIL|REVIEW (internal)
    private String message;      // reason/message

    // addressing (for traceability)
    private String bucket;
    private String frontKey;
    private String backKey;
}

