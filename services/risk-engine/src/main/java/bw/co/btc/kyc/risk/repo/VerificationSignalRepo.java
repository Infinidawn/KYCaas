package bw.co.btc.kyc.risk.repo;

import bw.co.btc.kyc.risk.entity.VerificationSignal;
import bw.co.btc.kyc.risk.enumeration.SignalSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VerificationSignalRepo extends JpaRepository<VerificationSignal, UUID> {

    Optional<VerificationSignal> findBySessionIdAndSource(UUID sessionId, SignalSource source);

    boolean existsBySessionIdAndSource(UUID sessionId, SignalSource source);
}
