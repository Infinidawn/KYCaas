package bw.co.btc.kyc.risk.service;

import bw.co.btc.kyc.risk.dto.KycDecisionDto;
import bw.co.btc.kyc.risk.entity.KycDecision;
import bw.co.btc.kyc.risk.entity.VerificationSignal;
import bw.co.btc.kyc.risk.enumeration.SignalSource;
import bw.co.btc.kyc.risk.messaging.KycDecisionPublisher;
import bw.co.btc.kyc.risk.repo.KycDecisionRepo;
import bw.co.btc.kyc.risk.repo.VerificationSignalRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionService {

    private final VerificationSignalRepo signalRepo;
    private final KycDecisionRepo decisionRepo;
    private final ObjectMapper om;
    private final KycDecisionPublisher publisher;

    /**
     * Recompute aggregated decision for a session by reading the latest signals we store.
     * For now we only aggregate OCR (DOCUMENT). Biometric slots are left null.
     *
     * Rules (OCR-only baseline):
     *  - If any FAIL        -> overall FAIL
     *  - Else if any REVIEW -> overall REVIEW
     *  - Else if any PASS   -> overall PASS
     *  - Else overall null (no signals yet)
     *
     * We also try to collect "reasons" from the OCR payload_json if present.
     */
    public void recompute(UUID sessionId, UUID tenantId) {
        // --- Fetch the latest OCR (DOCUMENT) signal for this session ---
        Optional<VerificationSignal> docOpt =
                signalRepo.findBySessionIdAndSource(sessionId, SignalSource.DOCUMENT);

        String ocrStatus = null;
        Double ocrScore = null;
        List<String> reasons = new ArrayList<>();

        if (docOpt.isPresent()) {
            var s = docOpt.get();
            ocrStatus = safeUpper(s.getStatus() == null ? null : s.getStatus().name());
            ocrScore  = s.getScore();
            reasons.addAll(extractReasonsFromPayload(s.getPayloadJson()));
        }

        // --- Aggregate (OCR-only for now) ---
        String overall = aggregate(List.of(ocrStatus));

        // --- Build reasons_json (simple, flat list under "reasons") ---
        String reasonsJson = toReasonsJson(reasons);

        // --- Upsert KycDecision row (unique by session) ---
        KycDecision d = decisionRepo.findBySessionId(sessionId)
                .orElse(KycDecision.builder()
                        .sessionId(sessionId)
                        .tenantId(tenantId)
                        .build());

        d.setOcrStatus(ocrStatus);
        d.setOcrScore(ocrScore);

        // Biometric slice reserved for later
        // d.setBiometricStatus(...)
        // d.setBiometricScore(...)

        d.setOverallStatus(overall);
        d.setReasonsJson(reasonsJson);

        d = decisionRepo.save(d);

        log.info("[DECISION] Upserted sessionId={} overall={} ocrStatus={} ocrScore={}",
                sessionId, overall, ocrStatus, ocrScore);

        // --- Publish decision.updated event for consumers (onboarding/UI, etc.) ---
        KycDecisionDto dto = toDto(d);
        publisher.publish(dto);
    }

    // ---------------------- helpers ----------------------

    private String aggregate(List<String> statuses) {
        boolean hasFail   = statuses.stream().anyMatch(s -> "FAIL".equalsIgnoreCase(s));
        boolean hasReview = statuses.stream().anyMatch(s -> "REVIEW".equalsIgnoreCase(s));
        boolean hasPass   = statuses.stream().anyMatch(s -> "PASS".equalsIgnoreCase(s));

        if (hasFail)   return "FAIL";
        if (hasReview) return "REVIEW";
        if (hasPass)   return "PASS";
        return null; // nothing yet
    }

    private String toReasonsJson(List<String> reasons) {
        try {
            Map<String, Object> body = Map.of("reasons", reasons == null ? List.of() : reasons);
            return om.writeValueAsString(body);
        } catch (Exception e) {
            return "{\"reasons\":[]}";
        }
    }

    private String safeUpper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Try to extract reasons from a stored payload_json.
     * Accepts:
     *   {"reasons":["LOW_QUALITY","MRZ_MISSING", ...]}
     * or just a free-form object where "reasons" may be nested; we only read root.reasons here.
     */
    private List<String> extractReasonsFromPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) return List.of();
        try {
            JsonNode root = om.readTree(payloadJson);
            JsonNode arr  = root.path("reasons");
            if (arr.isArray()) {
                List<String> out = new ArrayList<>();
                arr.forEach(n -> out.add(n.asText()));
                return out;
            }
        } catch (Exception ignore) {
            // swallow; reasons are optional
        }
        return List.of();
    }

    private KycDecisionDto toDto(KycDecision d) {
        String[] reasons = new String[0];
        try {
            if (d.getReasonsJson() != null && !d.getReasonsJson().isBlank()) {
                JsonNode n = om.readTree(d.getReasonsJson());
                JsonNode arr = n.isArray() ? n : n.path("reasons");
                if (arr.isArray()) {
                    reasons = new String[arr.size()];
                    for (int i = 0; i < arr.size(); i++) reasons[i] = arr.get(i).asText();
                }
            }
        } catch (Exception ignore) {}

        return KycDecisionDto.builder()
                .sessionId(d.getSessionId())
                .tenantId(d.getTenantId())
                .ocr(KycDecisionDto.Slice.builder()
                        .status(d.getOcrStatus())
                        .score(d.getOcrScore())
                        .build())
                .biometric(KycDecisionDto.Slice.builder()
                        .status(d.getBiometricStatus())
                        .score(d.getBiometricScore())
                        .build())
                .overall(d.getOverallStatus())
                .reasons(reasons)
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
