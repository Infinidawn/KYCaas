package bw.co.btc.kyc.onboarding.integration;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class KycDecisionDto {
    UUID sessionId;
    UUID tenantId;
    Slice ocr;
    Slice biometric;
    String overall;     // PASS | FAIL | REVIEW | null
    String[] reasons;   // may be empty
    Instant updatedAt;

    @Value
    @Builder
    public static class Slice {
        String status;  // PASS | FAIL | REVIEW | null
        Double score;
    }
}
