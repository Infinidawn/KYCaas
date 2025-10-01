package bw.co.btc.kyc.onboarding.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record UploadUrlsResponse(
        UUID sessionId,
        Instant issuedAt,
        Map<String, UploadItem> items
) {
    public record UploadItem(
            String method,
            String url,
            Map<String, String> headers,
            Instant expiresAt
    ) {}

}