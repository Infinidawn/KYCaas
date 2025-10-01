package bw.co.btc.kyc.biometrics.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * Optional event: signals when biometric metadata is saved (before processing).
 * Mirrors "document.metadata.saved".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BiometricMetadataSaved {
    private UUID sessionId;
    private UUID tenantId;
    private Map<String, String> objects; // e.g., { "selfie": "key1", "video": "key2" }
}