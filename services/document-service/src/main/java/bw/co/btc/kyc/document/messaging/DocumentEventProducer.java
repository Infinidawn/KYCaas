package bw.co.btc.kyc.document.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DocumentEventProducer {
    private final RabbitTemplate rabbit;
    private final String exchange;
    private final String rkMetadataSaved;
    private final String rkOcrCompleted;

    public DocumentEventProducer(RabbitTemplate rabbit,
                                 @Value("${app.document.exchange:x.document}") String exchange,
                                 @Value("${app.document.rk.metadataSaved:document.metadata.saved}") String rkMetadataSaved,
                                 @Value("${app.document.rk.ocrCompleted:document.ocr.completed}") String rkOcrCompleted) {
        this.rabbit = rabbit;
        this.exchange = exchange;
        this.rkMetadataSaved = rkMetadataSaved;
        this.rkOcrCompleted = rkOcrCompleted;
    }

    public void emitMetadataSaved(Object payload) {
        rabbit.convertAndSend(exchange, rkMetadataSaved, payload);
    }

    public void emitOcrCompleted(Object payload) {
        rabbit.convertAndSend(exchange, rkOcrCompleted, payload);
    }
}
