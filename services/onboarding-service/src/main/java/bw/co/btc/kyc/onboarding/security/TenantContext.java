package bw.co.btc.kyc.onboarding.security;

import java.util.UUID;

/**
 * Thread-local tenant context.
 *
 * - Populated per request by TenantFilter
 * - Cleared at the end of request to avoid leakage
 * - Use TenantContext.getTenantId() inside services to know "who the caller is"
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> TENANT_NAME = new ThreadLocal<>();

    private TenantContext() { }

    public static void set(UUID tenantId, String tenantName) {
        TENANT_ID.set(tenantId);
        TENANT_NAME.set(tenantName);
    }

    public static UUID getTenantId() {
        return TENANT_ID.get();
    }

    public static String getTenantName() {
        return TENANT_NAME.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        TENANT_NAME.remove();
    }
}

