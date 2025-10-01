package bw.co.btc.kyc.onboarding.service;

import bw.co.btc.kyc.onboarding.entity.KycStageLog;
import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import bw.co.btc.kyc.onboarding.repo.KycStageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KycOrchestratorService {

    private final KycSessionRepo sessions;
    private final KycStageLogRepository logs;


    public void markState(UUID sessionId, KycSessionState state) {
        sessions.findById(sessionId).ifPresentOrElse(s -> {
            s.setState(state.name());
            sessions.save(s);
            stageLog(sessionId, "STATE_CHANGED", "state=" + state.name());
        }, () -> log.warn("[Onboarding] session not found: {}", sessionId));
    }

    public void stageLog(UUID sessionId, String event, String details) {
        logs.save(KycStageLog.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .event(event)
                .details(details)
                .createdAt(Instant.now())
                .build());
        log.info("[Onboarding] stageLog session={} event={} details={}", sessionId, event, details);
    }
}
