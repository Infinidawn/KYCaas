package bw.co.btc.kyc.risk.api;

import bw.co.btc.kyc.risk.entity.VerificationSignal;
import bw.co.btc.kyc.risk.enumeration.SignalSource;
import bw.co.btc.kyc.risk.repo.VerificationSignalRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Read-only endpoints to inspect raw verification signals (for debugging/audit).
 *
 * Usage:
 *   GET /risk/signals/{sessionId}?source=DOCUMENT
 *   GET /risk/signals/{sessionId}?source=BIOMETRIC   (when biometric is added)
 *
 * We intentionally require "source" so we don't need a list-by-session repo method.
 */
@RestController
@RequestMapping("/risk/signals")
@RequiredArgsConstructor
public class SignalsController {

    private final VerificationSignalRepo repo;
    private final ObjectMapper om;

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getBySessionAndSource(@PathVariable UUID sessionId,
                                                   @RequestParam SignalSource source) {
        Optional<VerificationSignal> sigOpt = repo.findBySessionIdAndSource(sessionId, source);
        if (sigOpt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toDto(sigOpt.get()));
    }

    private SignalDto toDto(VerificationSignal s) {
        JsonNode payload = null;
        try {
            if (s.getPayloadJson() != null && !s.getPayloadJson().isBlank()) {
                payload = om.readTree(s.getPayloadJson());
            }
        } catch (Exception ignore) {
            // If malformed, leave payload as null; clients can fetch raw via /audit if needed
        }
        return new SignalDto(
                s.getSessionId(),
                s.getTenantId(),
                s.getSource().name(),
                s.getStatus().name(),
                s.getScore(),
                payload,
                s.getUpdatedAt()
        );
    }

    @Value
    public static class SignalDto {
        UUID sessionId;
        UUID tenantId;
        String source;       // DOCUMENT / BIOMETRIC / ...
        String status;       // PASS / FAIL / REVIEW
        Double score;        // nullable
        JsonNode payload;    // parsed from payload_json (nullable if parse failed)
        Instant updatedAt;
    }
}
