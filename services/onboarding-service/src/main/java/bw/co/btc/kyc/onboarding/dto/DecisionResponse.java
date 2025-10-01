package bw.co.btc.kyc.onboarding.dto;


import java.time.Instant;
import java.util.List;

public record DecisionResponse(
        String status,
        List<String> reasons,
        Instant decidedAt
) {}
