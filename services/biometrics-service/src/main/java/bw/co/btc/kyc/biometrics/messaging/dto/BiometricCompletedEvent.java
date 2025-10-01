package bw.co.btc.kyc.biometrics.messaging.dto;

import lombok.*; import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiometricCompletedEvent {
    private UUID sessionId;
    private UUID tenantId;
    private String status;         // SUCCESS | FAILED
    private Double livenessScore;
    private Double faceMatchScore;
    private String message;
}
