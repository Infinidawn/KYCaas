package bw.co.btc.kyc.onboarding.dto;

import java.time.Instant;
import java.util.UUID;

public record StartSessionResponse (
    UUID sessionId,
    Instant createdAt
){}
