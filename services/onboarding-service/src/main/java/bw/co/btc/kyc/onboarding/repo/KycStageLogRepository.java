package bw.co.btc.kyc.onboarding.repo;

import bw.co.btc.kyc.onboarding.entity.KycStageLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.UUID;

public interface KycStageLogRepository extends JpaRepository<KycStageLog, UUID> {
    List<KycStageLog> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Page<KycStageLog> findBySessionIdOrderByCreatedAtAsc(UUID sessionId, Pageable pageable);
}
