package bw.co.btc.kyc.biometrics.dto;

import java.util.UUID;

public record BiometricResultEvent(
        UUID sessionId,
        UUID tenantId,
        String status,     // MATCHED | MISMATCHED
        double score,
        String message
) {}
