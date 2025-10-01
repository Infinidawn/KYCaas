package bw.co.btc.kyc.risk.api;

import bw.co.btc.kyc.risk.dto.KycDecisionDto;
import bw.co.btc.kyc.risk.entity.KycDecision;
import bw.co.btc.kyc.risk.repo.KycDecisionRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/risk/decision")
@RequiredArgsConstructor
public class KycDecisionController {

    private final KycDecisionRepo repo;
    private final ObjectMapper om;

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> get(@PathVariable UUID sessionId,
                                 @RequestParam(defaultValue = "aggregated") String view) {
        return repo.findBySessionId(sessionId)
                .<ResponseEntity<?>>map(d -> ResponseEntity.ok(toDto(d)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private KycDecisionDto toDto(KycDecision d) {
        String[] reasons = extractReasons(d.getReasonsJson());

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

    private String[] extractReasons(String reasonsJson) {
        if (reasonsJson == null || reasonsJson.isBlank()) return new String[0];
        try {
            JsonNode root = om.readTree(reasonsJson);
            // Accept either {"reasons":[...]} or a plain array [...]
            JsonNode arr = root.isArray() ? root : root.path("reasons");
            List<String> out = new ArrayList<>();
            if (arr.isArray()) arr.forEach(n -> out.add(n.asText()));
            return out.toArray(String[]::new);
        } catch (Exception ignore) {
            return new String[0];
        }
    }
}
