package bw.co.btc.kyc.biometrics.simulator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BiometricResultSim {
    private UUID sessionId;
    private UUID tenantId;
    private String selfieUrl;
    private String videoUrl;

    private String status;         // MATCHED | MISMATCHED
    private Double faceMatchScore; // e.g., 0.92
    private Double livenessScore;  // e.g., 0.88
    private boolean livenessPassed;

    private String message;
}