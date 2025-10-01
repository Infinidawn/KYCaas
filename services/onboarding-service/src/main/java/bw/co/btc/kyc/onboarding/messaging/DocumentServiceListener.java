package bw.co.btc.kyc.onboarding.messaging;

import bw.co.btc.kyc.onboarding.enumeration.KycSessionState;
import bw.co.btc.kyc.onboarding.messaging.dto.DocumentMetadataSaved;
import bw.co.btc.kyc.onboarding.messaging.dto.OcrCompletedEvent;
import bw.co.btc.kyc.onboarding.service.KycOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentServiceListener {

    private final KycOrchestratorService orchestrator;

    /** document-service → metadata saved (OCR starts inside document-service itself) */
    @RabbitListener(queues = "${app.queues.document.metadataSaved:q.onboarding.document.metadata.saved}")
    public void onMetadataSaved(DocumentMetadataSaved evt) {
        log.info("[Onboarding<-DocumentService] metadata.saved sessionId={} keys={}",
                evt.getSessionId(), evt.getObjects().keySet());
        orchestrator.markState(evt.getSessionId(), KycSessionState.DOCS_SUBMITTED);
        orchestrator.stageLog(evt.getSessionId(), "DOCS_SUBMITTED",
                "objects=" + evt.getObjects().keySet());
    }

    /** document-service → OCR completed */
    @RabbitListener(queues = "${app.queues.document.ocrCompleted:q.onboarding.document.ocr.completed}")
    public void onOcrCompleted(OcrCompletedEvent evt) {
        log.info("[Onboarding<-DocumentService] ocr.completed sessionId={} status={}",
                evt.getSessionId(), evt.getStatus());
        orchestrator.markState(evt.getSessionId(), KycSessionState.OCR_COMPLETED);
        orchestrator.stageLog(evt.getSessionId(), "OCR_COMPLETED", "status=" + evt.getStatus());
    }
}
