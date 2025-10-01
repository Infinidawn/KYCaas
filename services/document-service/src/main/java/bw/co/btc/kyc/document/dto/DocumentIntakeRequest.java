package bw.co.btc.kyc.document.dto;

import bw.co.btc.kyc.document.enumeration.IdType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Client submits document metadata + where files were uploaded.
 * Either provide bucket+keys OR presigned URLs that contain /{bucket}/{objectKey}.
 */
public record DocumentIntakeRequest(
        @NotNull IdType idType,
        @NotBlank @Size(min = 2, max = 64) String idNumber,

        // v1 style: parse to bucket/objectKey
        String frontImageUrl,
        String backImageUrl,

        // preferred: explicit addressing
        String bucket,
        String frontObjectKey,
        String backObjectKey
) {}
