package bw.co.btc.kyc.onboarding.repo;
import bw.co.btc.kyc.onboarding.entity.KycSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface KycSessionRepo extends JpaRepository<KycSession, UUID> {}
