package bw.co.btc.kyc.risk.api;

import bw.co.btc.kyc.risk.entity.KycDecision;
import bw.co.btc.kyc.risk.repo.KycDecisionRepo;
import bw.co.btc.kyc.risk.service.DecisionService;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController @RequestMapping("/internal/v1")
public class DecisionController {
    private final DecisionService svc; private final KycDecisionRepo repo;
    public DecisionController(DecisionService svc, KycDecisionRepo repo){ this.svc=svc; this.repo=repo; }


    @GetMapping("/sessions/{sessionId}/decision")
    public KycDecision get(@PathVariable UUID sessionId){ return repo.findById(sessionId).orElse(null); }
}