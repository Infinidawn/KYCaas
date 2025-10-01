package bw.co.btc.kyc.risk.messaging;

import bw.co.btc.kyc.risk.dto.KycDecisionDto;
import bw.co.btc.kyc.risk.entity.KycDecision;
import bw.co.btc.kyc.risk.messaging.dto.BiometricCompletedEvent;
import bw.co.btc.kyc.risk.repo.KycDecisionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiometricCompletedListener {

    private final KycDecisionRepo decisions;
    private final KycDecisionPublisher publisher;

    @Transactional
    @RabbitListener(queues = "${app.queues.risk.biometricCompleted:q.risk.biometric.completed}")
    public void onBiometricCompleted(BiometricCompletedEvent evt) {
        var d = decisions.findBySessionId(evt.getSessionId()).orElseGet(() ->
                KycDecision.builder().sessionId(evt.getSessionId()).tenantId(evt.getTenantId()).build()
        );

        String bioStatus = "SUCCESS".equalsIgnoreCase(evt.getStatus()) ? "PASS" : "FAIL";
        d.setBiometricStatus(bioStatus);
        // choose a composite score; or keep one
        d.setBiometricScore(avg(evt.getLivenessScore(), evt.getFaceMatchScore()));

        // aggregate: any FAIL → FAIL; else if any REVIEW → REVIEW; else PASS
        String overall = aggregate(d);
        d.setOverallStatus(overall);
        d.setUpdatedAt(Instant.now());
        decisions.save(d);

        var outbound = new KycDecisionDto(
                d.getSessionId(),
                d.getTenantId(),
                new KycDecisionDto.Slice(d.getOcrStatus(), d.getOcrScore()),
                new KycDecisionDto.Slice(d.getBiometricStatus(), d.getBiometricScore()),
                mapOverallToEnum(overall),
                new String[0],
                d.getUpdatedAt()
        );
        publisher.publish(outbound);
        log.info("[Risk] biometric.completed → decision.updated sessionId={} overall={}", d.getSessionId(), outbound.getOverall());
    }

    private static Double avg(Double a, Double b) {
        if (a == null) return b;
        if (b == null) return a;
        return (a + b) / 2.0;
    }

    private String aggregate(KycDecision d) {
        if ("FAIL".equalsIgnoreCase(d.getOcrStatus()) || "FAIL".equalsIgnoreCase(d.getBiometricStatus())) return "FAIL";
        if ("PASS".equalsIgnoreCase(d.getOcrStatus()) && "PASS".equalsIgnoreCase(d.getBiometricStatus())) return "PASS";
        return "REVIEW";
    }
    private String mapOverallToEnum(String s) {
        return switch (s == null ? "" : s.toUpperCase()) {
            case "PASS", "APPROVED" -> "APPROVED";
            case "FAIL", "REJECTED" -> "REJECTED";
            default -> "REVIEW";
        };
    }
}
