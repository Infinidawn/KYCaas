package bw.co.btc.kyc.onboarding.messaging;

import bw.co.btc.kyc.onboarding.entity.KycSession;
import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.messaging.dto.DocumentMetadataSaved;
import bw.co.btc.kyc.onboarding.messaging.dto.OcrCompletedEvent;
import bw.co.btc.kyc.onboarding.repo.KycSessionRepo;
import bw.co.btc.kyc.onboarding.repo.KycStageLogRepository;
import bw.co.btc.kyc.onboarding.service.KycOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentServiceListener {

    private final KycSessionRepo sessions;
    private final KycStageLogRepository stageLogs;
    private final KycOrchestratorService orchestrator;

    /** document-service → metadata saved */
    @RabbitListener(queues = "${app.queues.document.metadataSaved:q.onboarding.document.metadata.saved}")
    public void onMetadataSaved(DocumentMetadataSaved evt) {
        UUID sessionId = evt.getSessionId();

        sessions.findById(sessionId).ifPresentOrElse(s -> {
            if (!tenantMatches(s, evt.getTenantId())) return;

            updateStateIfNeeded(s, KycSessionState.DOCS_SUBMITTED);
            logStageIfNotExists(sessionId, "DOCS_SUBMITTED",
                    "objects=" + evt.getObjects().keySet());
            log.info("[Onboarding<-Document] metadata.saved sessionId={} tenant={} objects={}",
                    sessionId, s.getTenantId(), evt.getObjects().keySet());

        }, () -> log.warn("[Onboarding<-Document] metadata.saved for unknown sessionId={}", sessionId));
    }

    /** document-service → OCR completed */
    @RabbitListener(queues = "${app.queues.document.ocrCompleted:q.onboarding.document.ocr.completed}")
    public void onOcrCompleted(OcrCompletedEvent evt) {
        UUID sessionId = evt.getSessionId();

        sessions.findById(sessionId).ifPresentOrElse(s -> {
            if (!tenantMatches(s, evt.getTenantId())) return;

            updateStateIfNeeded(s, KycSessionState.OCR_COMPLETED);
            logStageIfNotExists(sessionId, "OCR_COMPLETED", "status=" + evt.getStatus());
            log.info("[Onboarding<-Document] ocr.completed sessionId={} tenant={} status={}",
                    sessionId, s.getTenantId(), evt.getStatus());

        }, () -> log.warn("[Onboarding<-Document] ocr.completed for unknown sessionId={}", sessionId));
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
}
