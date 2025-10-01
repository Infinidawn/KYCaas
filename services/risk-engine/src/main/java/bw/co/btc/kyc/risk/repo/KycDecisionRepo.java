package bw.co.btc.kyc.risk.repo;

import bw.co.btc.kyc.risk.entity.KycDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycDecisionRepo extends JpaRepository<KycDecision, UUID> {

    Optional<KycDecision> findBySessionId(UUID sessionId);

    boolean existsBySessionId(UUID sessionId);
}