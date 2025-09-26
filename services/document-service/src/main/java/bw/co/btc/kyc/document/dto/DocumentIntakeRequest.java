package bw.co.btc.kyc.document.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record DocumentIntakeRequest(
        UUID sessionId,
        @NotBlank String idType,
        String idNumber,

        // v1 (legacy): URLs
        String frontImageUrl,
        String backImageUrl,

        // v2 (preferred): bucket + objectKey
        String bucket,
        String objectKey
) {}
