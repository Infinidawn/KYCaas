package bw.co.btc.kyc.onboarding.repo;


import bw.co.btc.kyc.onboarding.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for SaaS tenants.
 *
 * Naming:
 *  - Repository names mirror entity names (Tenant -> TenantRepository)
 *  - Keep only targeted finders to avoid bloat.
 */

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /** Unique by name (enforced by DB constraint). */
    Optional<Tenant> findByNameIgnoreCase(String name);
}
