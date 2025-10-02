package bw.co.btc.kyc.onboarding.messaging;

import bw.co.btc.kyc.onboarding.entity.KycSession;
import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import bw.co.btc.kyc.onboarding.repo.KycStageLogRepository;
import bw.co.btc.kyc.onboarding.service.KycOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiometricServiceListener {

    private final KycSessionRepo sessions;
    private final KycStageLogRepository stageLogs;
    private final KycOrchestratorService orchestrator;

    // DTOs for deserialization
    public static class BiometricMetadataSaved {
        public UUID sessionId;
        public UUID tenantId;
        public Map<String, String> objects;
    }

    public static class BiometricCompletedEvent {
        public UUID sessionId;
        public UUID tenantId;
        public String status;         // SUCCESS | FAILED
        public Double livenessScore;
        public Double faceMatchScore;
        public String message;
    }

    /** biometric-service → metadata saved */
    @RabbitListener(queues = "${app.queues.onboarding.biometricMetadataSaved:q.onboarding.biometric.metadata.saved}")
    public void onMetadataSaved(BiometricMetadataSaved evt) {
        UUID sessionId = evt.sessionId;

        sessions.findById(sessionId).ifPresentOrElse(s -> {
            if (!tenantMatches(s, evt.tenantId)) return;

            updateStateIfNeeded(s, KycSessionState.BIOMETRIC_SUBMITTED);
            logStageIfNotExists(sessionId, "BIOMETRIC_SUBMITTED",
                    "objects=" + (evt.objects == null ? "[]" : evt.objects.keySet()));
            log.info("[Onboarding<-Biometric] metadata.saved sessionId={} tenant={} objects={}",
                    sessionId, s.getTenantId(), evt.objects != null ? evt.objects.keySet() : "[]");

        }, () -> log.warn("[Onboarding<-Biometric] metadata.saved for unknown sessionId={}", sessionId));
    }

    /** biometric-service → completed */
    @RabbitListener(queues = "${app.queues.onboarding.biometricCompleted:q.onboarding.biometric.completed}")
    public void onCompleted(BiometricCompletedEvent evt) {
        UUID sessionId = evt.sessionId;

        sessions.findById(sessionId).ifPresentOrElse(s -> {
            if (!tenantMatches(s, evt.tenantId)) return;

            updateStateIfNeeded(s, KycSessionState.BIOMETRIC_COMPLETED);
            String details = "status=%s, live=%.2f, match=%.2f"
                    .formatted(evt.status, nz(evt.livenessScore), nz(evt.faceMatchScore));
            logStageIfNotExists(sessionId, "BIOMETRIC_COMPLETED", details);
            log.info("[Onboarding<-Biometric] completed sessionId={} tenant={} status={} live={} match={}",
                    sessionId, s.getTenantId(), evt.status, evt.livenessScore, evt.faceMatchScore);

        }, () -> log.warn("[Onboarding<-Biometric] completed for unknown sessionId={}", sessionId));
    }

    // --------- helpers ---------
    private boolean tenantMatches(KycSession session, UUID evtTenant) {
        if (evtTenant != null && !evtTenant.equals(session.getTenantId())) {
            log.error("[Onboarding] Tenant mismatch sessionId={} evtTenant={} sessionTenant={}",
                    session.getId(), evtTenant, session.getTenantId());
            return false;
        }
        return true;
    }

    private void updateStateIfNeeded(KycSession s, KycSessionState newState) {
        if (!newState.name().equals(s.getState())) {
            s.setState(newState.name());
            sessions.save(s);
        }
    }

    private void logStageIfNotExists(UUID sessionId, String event, String details) {
        boolean already = stageLogs.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream().anyMatch(l -> event.equals(l.getEvent()));
        if (!already) {
            orchestrator.stageLog(sessionId, event, details);
        }
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }
}
