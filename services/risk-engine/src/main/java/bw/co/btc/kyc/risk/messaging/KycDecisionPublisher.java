package bw.co.btc.kyc.risk.messaging;

import bw.co.btc.kyc.risk.dto.KycDecisionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KycDecisionPublisher {

    private final RabbitTemplate rabbit;

    @Value("${app.exchanges.risk:x.risk}")
    private String riskExchange;

    @Value("${app.rk.decisionUpdated:decision.updated}")
    private String decisionUpdatedRoutingKey;

    /** Publish aggregated decision for a session. */
    public void publish(KycDecisionDto dto) {
        rabbit.convertAndSend(riskExchange, decisionUpdatedRoutingKey, dto);
        log.info("[Risk->{}:{}] decision.updated sessionId={} overall={}",
                riskExchange, decisionUpdatedRoutingKey, dto.getSessionId(), dto.getOverall());
    }
}