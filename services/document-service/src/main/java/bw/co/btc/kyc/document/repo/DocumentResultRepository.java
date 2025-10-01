package bw.co.btc.kyc.document.repo;
import bw.co.btc.kyc.document.entity.DocumentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; import java.util.UUID;
public interface DocumentResultRepository extends JpaRepository<DocumentResult, UUID> {
  List<DocumentResult> findBySessionId(UUID sessionId);
}
