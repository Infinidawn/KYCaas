package bw.co.btc.kyc.risk.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class KycDecisionDto {
    private UUID sessionId;
    private UUID tenantId;
    private Slice ocr;
    private Slice biometric;
    private String overall;
    private String[] reasons;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Slice {
        private String status;
        private Double score;
    }
}

