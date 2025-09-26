package bw.co.btc.kyc.document.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Map;

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
