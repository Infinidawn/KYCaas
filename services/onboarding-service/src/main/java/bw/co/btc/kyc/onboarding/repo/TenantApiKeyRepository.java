package bw.co.btc.kyc.onboarding.repo;


import bw.co.btc.kyc.onboarding.entity.TenantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant API keys.
 *
 * Notes:
 *  - Key lookup by keyId (UUID) is required for request-time auth.
 *  - We only expose methods that make sense for validation / revocation.
 */
public interface TenantApiKeyRepository extends JpaRepository<TenantApiKey, UUID> {

    /**
     * Lookup active key by its keyId.
     */
    Optional<TenantApiKey> findByKeyIdAndRevokedAtIsNull(UUID keyId);
}