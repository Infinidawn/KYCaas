package bw.co.btc.kyc.document.messaging.dto;

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
    private Double quality;  // optional
    private String message;  // optional
}
