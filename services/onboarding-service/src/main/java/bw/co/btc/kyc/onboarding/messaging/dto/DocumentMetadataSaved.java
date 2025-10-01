package bw.co.btc.kyc.onboarding.messaging.dto;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class DocumentMetadataSaved {
    private UUID sessionId;
    private UUID tenantId;
    private Map<String,String> objects;
}