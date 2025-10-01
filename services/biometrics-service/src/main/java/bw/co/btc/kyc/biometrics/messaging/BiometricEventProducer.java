package bw.co.btc.kyc.biometrics.messaging;

import bw.co.btc.kyc.biometrics.messaging.dto.BiometricCompletedEvent;
import bw.co.btc.kyc.biometrics.messaging.dto.BiometricMetadataSaved;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


import java.util.Map;


/**
 * Emits biometric-related events onto RabbitMQ exchanges.
 *  - metadata.saved
 *  - biometric.completed
 */
@Component
@RequiredArgsConstructor
public class BiometricEventProducer {

    private final RabbitTemplate rabbit;

    @Value("${app.biometric.exchange:x.biometric}")
    private String exchange;

    @Value("${app.biometric.rk.metadataSaved:biometric.metadata.saved}")
    private String rkMetadataSaved;

    @Value("${app.biometric.rk.completed:biometric.completed}")
    private String rkCompleted;

    public void emitMetadataSaved(BiometricMetadataSaved evt) {
        rabbit.convertAndSend(exchange, rkMetadataSaved, evt);
    }

    public void emitBiometricCompleted(BiometricCompletedEvent evt) {
        rabbit.convertAndSend(exchange, rkCompleted, evt);
    }
}