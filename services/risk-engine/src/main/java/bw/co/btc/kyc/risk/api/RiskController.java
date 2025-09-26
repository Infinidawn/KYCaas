package bw.co.btc.kyc.risk.api;

import bw.co.btc.kyc.risk.domain.Decision;
import bw.co.btc.kyc.risk.repo.DecisionRepo;
import bw.co.btc.kyc.risk.repo.DocBioViews;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1")
public class RiskController {
  private final DecisionRepo decisions;
  private final DocBioViews views;

  public RiskController(DecisionRepo d, DocBioViews v) {
    this.decisions = d;
    this.views = v;
  }

  @PostMapping("/decide/{sessionId}")
  public ResponseEntity<?> decide(@PathVariable("sessionId") UUID sessionId) {

    int idLen = views.maxIdNumberLen(sessionId);

    int selfies = views.selfieCount(sessionId);

    String status = "REVIEW";
    String reasons = "";

    if (idLen == 0) {
      status = "AUTO_REJECTED";
      reasons = "ID_NUMBER_INVALID";
    } else if (selfies > 0) {
      status = "AUTO_APPROVED";
    } else {
      status = "REVIEW";
      reasons = "INSUFFICIENT_EVIDENCE";
    }
    Decision d = Decision.builder().session_id(sessionId)
            .tenant_id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .status(status).reasons(reasons).created_at(Instant.now()).build();
    decisions.save(d);
    return ResponseEntity.ok(Map.of("sessionId", sessionId.toString(), "status", status, "reasons", reasons.isBlank() ? new String[]{} : reasons.split(",")));
  }
}
