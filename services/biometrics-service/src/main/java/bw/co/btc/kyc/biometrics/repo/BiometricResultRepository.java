package bw.co.btc.kyc.biometrics.repo;
import bw.co.btc.kyc.biometrics.entity.BiometricResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface BiometricResultRepository extends JpaRepository<BiometricResult, UUID> {
}