package bw.co.btc.kyc.risk.repo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class DocBioViews {

    @PersistenceContext
    private EntityManager em;

    public int maxIdNumberLen(UUID sessionId) {
        Number n = (Number) em.createNativeQuery(
                        "select coalesce(max(length(id_number)), 0) " +
                                "from document_result where session_id = ?1")
                .setParameter(1, sessionId)   // 1-based index
                .getSingleResult();
        return n.intValue();
    }

    public int selfieCount(UUID sessionId) {
        Number n = (Number) em.createNativeQuery(
                        "select count(1) from biometric_result where session_id = ?1")
                .setParameter(1, sessionId)   // 1-based index
                .getSingleResult();
        return n.intValue();
    }
}
