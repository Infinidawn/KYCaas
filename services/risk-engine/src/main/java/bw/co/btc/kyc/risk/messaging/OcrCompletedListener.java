package bw.co.btc.kyc.risk.messaging;

import bw.co.btc.kyc.risk.dto.KycDecisionDto;
import bw.co.btc.kyc.risk.entity.KycDecision;
import bw.co.btc.kyc.risk.messaging.dto.OcrCompletedEvent;
import bw.co.btc.kyc.risk.repo.KycDecisionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OcrCompletedListener {

    private final KycDecisionRepo decisions;
    private final KycDecisionPublisher publisher; // existing publisher in your project

    /**
     * Consume OCR completion from document-service (x.document:routingKey=document.ocr.completed).
     * Upsert OCR slice, aggregate overall, persist, then publish decision.updated to x.risk.
     */
    @Transactional
    @RabbitListener(queues = "${app.queues.risk.ocrCompleted:q.risk.document.ocr.completed}")
    public void onOcrCompleted(OcrCompletedEvent evt) {
        UUID sessionId = evt.getSessionId();
        log.info("[Risk] document.ocr.completed sessionId={} status={} quality={}",
                sessionId, evt.getStatus(), evt.getQuality());

        // --- Upsert aggregated decision row for this session ---
        KycDecision d = decisions.findBySessionId(sessionId).orElseGet(() ->
                KycDecision.builder()
                        .sessionId(sessionId)
                        .tenantId(evt.getTenantId())
                        .build()
        );

        // Map "SUCCESS"/"FAILED" to OCR slice PASS/FAIL
        String ocrStatus = "SUCCESS".equalsIgnoreCase(evt.getStatus()) ? "PASS" : "FAIL";
        d.setOcrStatus(ocrStatus);
        d.setOcrScore(evt.getQuality());

        // --- Aggregate overall (phase 1: OCR-only) ---
        String overall = aggregateOverall(d); // PASS | FAIL | REVIEW
        d.setOverallStatus(overall);
        d.setReasonsJson(buildReasons(overall, evt.getMessage()));
        d.setUpdatedAt(Instant.now());

        decisions.save(d);

        // --- Publish decision.updated for onboarding ---
        var outbound = new KycDecisionDto(
                d.getSessionId(),
                d.getTenantId(),
                new KycDecisionDto.Slice(d.getOcrStatus(), d.getOcrScore()),
                new KycDecisionDto.Slice(d.getBiometricStatus(), d.getBiometricScore()),
                mapOverallToEnum(overall),              // APPROVED | REJECTED | REVIEW
                extractReasonsArray(d.getReasonsJson()),
                d.getUpdatedAt()
        );

        publisher.publish(outbound);
        log.info("[Risk->x.risk] decision.updated sessionId={} overall={}", outbound.getSessionId(), outbound.getOverall());
    }

    // --- simple aggregation (extend when biometric/sanctions are added) ---
    private String aggregateOverall(KycDecision d) {
        if ("FAIL".equalsIgnoreCase(d.getOcrStatus())) return "FAIL";
        if ("PASS".equalsIgnoreCase(d.getOcrStatus())) return "PASS";
        return "REVIEW";
    }

    private String mapOverallToEnum(String overall) {
        return switch (overall == null ? "" : overall.toUpperCase()) {
            case "PASS", "APPROVED" -> "APPROVED";
            case "FAIL", "REJECTED" -> "REJECTED";
            default -> "REVIEW";
        };
    }

    private String buildReasons(String overall, String msg) {
        // very small JSON array string (keep simple for now)
        String base = (overall == null || overall.isBlank()) ? "UNKNOWN" : overall.toUpperCase();
        if (msg == null || msg.isBlank()) return "[\"" + base + "\"]";
        String safe = msg.replace("\"", "\\\"");
        return "[\"" + base + "\",\"" + safe + "\"]";
    }

    private String[] extractReasonsArray(String reasonsJson) {
        // Keep minimal: return empty until you need rich reasons
        return new String[0];
    }
}