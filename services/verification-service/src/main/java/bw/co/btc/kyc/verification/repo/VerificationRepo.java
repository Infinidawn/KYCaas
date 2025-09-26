package bw.co.btc.kyc.verification.repo;
import bw.co.btc.kyc.verification.domain.Verification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface VerificationRepo extends JpaRepository<Verification, UUID> {}
