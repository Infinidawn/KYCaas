package bw.co.btc.kyc.onboarding.dto;

import java.time.Instant;
import java.util.UUID;

public record StageLogDto(
        UUID id,
        UUID sessionId,
        String event,
        String details,
        Instant createdAt
) {}
