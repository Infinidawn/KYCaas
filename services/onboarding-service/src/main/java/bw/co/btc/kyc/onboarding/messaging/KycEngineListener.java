package bw.co.btc.kyc.onboarding.messaging;

import bw.co.btc.kyc.onboarding.entity.KycDecision;
import bw.co.btc.kyc.onboarding.entity.KycStageLog;
import bw.co.btc.kyc.onboarding.enumeration.KycDecisionStatus;
import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.integration.KycDecisionDto;
import bw.co.btc.kyc.onboarding.repo.KycDecisionRepository;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import bw.co.btc.kyc.onboarding.repo.KycStageLogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KycEngineListener {

    private final KycSessionRepo sessions;
    private final KycDecisionRepository decisions;
    private final KycStageLogRepository stageLogs;

    /** risk-engine → aggregated decision */
    @Transactional
    @RabbitListener(queues = "${app.queues.risk.decisionUpdated:q.onboarding.risk.decision.updated}")
    public void onDecisionUpdated(KycDecisionDto dto) {
        UUID sessionId = dto.getSessionId();
        UUID tenantId = dto.getTenantId();
        String overall = dto.getOverall() == null ? "" : dto.getOverall().trim().toUpperCase();

        KycDecisionStatus status;
        try {
            status = KycDecisionStatus.valueOf(overall);
        } catch (IllegalArgumentException e) {
            log.error("[Onboarding<-RiskEngine] Unknown overall='{}' for sessionId={}", dto.getOverall(), sessionId);
            return;
        }

        String reasonsCsv = (dto.getReasons() == null || dto.getReasons().length == 0)
                ? null : String.join(",", dto.getReasons());

        log.info("[Onboarding<-RiskEngine] decision.updated sessionId={} overall={} reasons={}",
                sessionId, status, reasonsCsv);

        // Upsert into risk_engine.kyc_decision (by sessionId PK)
        Optional<KycDecision> existing = decisions.findBySessionId(sessionId);
        KycDecision saved = existing.map(d -> {
            d.setStatus(status);
            d.setReasons(reasonsCsv);
            return decisions.save(d);
        }).orElseGet(() -> {
            var d = KycDecision.builder()
                    .sessionId(sessionId)
                    .tenantId(tenantId)
                    .status(status)
                    .reasons(reasonsCsv)
                    .createdAt(Instant.now())
                    .build();
            return decisions.save(d);
        });

        // Update onboarding session → DECIDED
        sessions.findById(sessionId).ifPresent(s -> {
            if (!KycSessionState.DECIDED.name().equals(s.getState())) {
                s.setState(KycSessionState.DECIDED.name());
                sessions.save(s);
            }
        });

        // Stage log (audit in onboarding DB)
        boolean alreadyLogged = stageLogs.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream().anyMatch(l -> "DECIDED".equals(l.getEvent()));
        if (!alreadyLogged) {
            stageLogs.save(new KycStageLog(
                    UUID.randomUUID(),
                    sessionId,
                    "DECIDED",
                    "status=" + saved.getStatus().name() +
                            (saved.getReasons() == null || saved.getReasons().isBlank() ? "" : ", reasons=" + saved.getReasons()),
                    Instant.now()
            ));
        }
    }
}
