package bw.co.btc.kyc.document.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * OcrProducer
 *
 * Responsibility:
 *  - Publish an OCR job request to RabbitMQ.
 *  - Uses a Topic Exchange and routing key (configurable) so consumers
 *    can bind queues flexibly (e.g., q.ocr.request).
 *
 * How it’s used:
 *  - Construct a payload (Map<String,Object>) with fields like:
 *      tenantId, sessionId, bucket, objectKey, idType, idNumber, requestedAt, ...
 *  - Call send(payload). The message will be serialized as JSON (see the
 *    Jackson message converter bean below) and delivered to the exchange.
 *
 * Notes for the team:
 *  - The exchange name and routing key are injected from properties:
 *      app.ocr.exchange (e.g., x.ocr)
 *      app.ocr.routingKey (e.g., ocr.request)
 *  - We attach a correlation id header for traceability across services.
 */

@Component
public class OcrProducer {
    private final RabbitTemplate rabbit;
    private final String exchange;
    private final String routingKey;

    public OcrProducer(RabbitTemplate rabbit,
                       @Value("${app.ocr.exchange}") String exchange,
                       @Value("${app.ocr.routingKey}") String routingKey) {
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }
    public void send(Map<String, Object> payload) {
        rabbit.convertAndSend(exchange, routingKey, payload);
    }
}
