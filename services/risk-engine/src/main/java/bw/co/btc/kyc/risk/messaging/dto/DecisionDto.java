package bw.co.btc.kyc.risk.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDto {
    UUID sessionId;
    UUID tenantId;
    String overall;   // APPROVED | REJECTED | REVIEW
    String[] reasons;
}
