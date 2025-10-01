package bw.co.btc.kyc.risk.messaging.dto;

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
    private Double quality;
    private String message;
}
