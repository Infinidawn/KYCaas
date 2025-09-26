package bw.co.btc.kyc.risk.repo;
import bw.co.btc.kyc.risk.domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface DecisionRepo extends JpaRepository<Decision, UUID> {}
