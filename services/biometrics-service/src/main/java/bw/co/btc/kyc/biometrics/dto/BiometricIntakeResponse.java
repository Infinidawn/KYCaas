package bw.co.btc.kyc.biometrics.dto;

import java.util.UUID;

public record BiometricIntakeResponse(
        boolean accepted,
        UUID correlationId
) {}
