package bw.co.btc.kyc.onboarding.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrCompletedEvent {
    private UUID sessionId;
    private UUID tenantId;
    private String status;   // SUCCESS | FAILED
    private String message;  // optional
    private Double quality;  // optional
}
