package bw.co.btc.kyc.biometrics.repo;
import bw.co.btc.kyc.biometrics.domain.BiometricResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface BiometricRepo extends JpaRepository<BiometricResult, UUID> {
  List<BiometricResult> findBySessionId(UUID sessionId);
}
