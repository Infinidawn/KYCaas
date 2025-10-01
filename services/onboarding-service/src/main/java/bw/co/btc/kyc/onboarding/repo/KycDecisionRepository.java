package bw.co.btc.kyc.onboarding.repo;

import bw.co.btc.kyc.onboarding.entity.KycDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface KycDecisionRepository extends JpaRepository<KycDecision, UUID> {
    Optional<KycDecision> findBySessionId(UUID sessionId);
}
