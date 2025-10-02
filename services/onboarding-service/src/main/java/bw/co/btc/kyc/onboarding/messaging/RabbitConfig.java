package bw.co.btc.kyc.onboarding.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // ========== document-service ==========
    @Bean TopicExchange documentExchange() {
        return ExchangeBuilder.topicExchange("x.document").durable(true).build();
    }

    @Bean Queue qDocumentMetadataSaved() {
        return QueueBuilder.durable("q.onboarding.document.metadata.saved").build();
    }

    @Bean Binding bDocumentMetadataSaved() {
        return BindingBuilder
                .bind(qDocumentMetadataSaved())
                .to(documentExchange())
                .with("document.metadata.saved");
    }

    @Bean Queue qDocumentOcrCompleted() {
        return QueueBuilder.durable("q.onboarding.document.ocr.completed").build();
    }

    @Bean Binding bDocumentOcrCompleted() {
        return BindingBuilder
                .bind(qDocumentOcrCompleted())
                .to(documentExchange())
                .with("document.ocr.completed");
    }

    // ========== biometric-service ==========
    @Bean TopicExchange biometricExchange() {
        return ExchangeBuilder.topicExchange("x.biometric").durable(true).build();
    }

    @Bean Queue qBiometricMetadataSaved() {
        return QueueBuilder.durable("q.onboarding.biometric.metadata.saved").build();
    }

    @Bean Binding bBiometricMetadataSaved(TopicExchange biometricExchange) {
        return BindingBuilder.bind(qBiometricMetadataSaved()).to(biometricExchange).with("biometric.metadata.saved");
    }

    @Bean Queue qBiometricCompleted() {
        return QueueBuilder.durable("q.onboarding.biometric.completed").build();
    }

    @Bean Binding bBiometricCompleted(TopicExchange biometricExchange) {
        return BindingBuilder.bind(qBiometricCompleted()).to(biometricExchange).with("biometric.completed");
    }


    // ========== risk-engine ==========
    @Bean TopicExchange riskExchange() {
        return ExchangeBuilder.topicExchange("x.risk").durable(true).build();
    }

    @Bean Queue qRiskDecisionUpdated() {
        return QueueBuilder.durable("q.onboarding.risk.decision.updated").build();
    }

    @Bean Binding bRiskDecisionUpdated() {
        return BindingBuilder
                .bind(qRiskDecisionUpdated())
                .to(riskExchange())
                .with("decision.updated");
    }

    // ========== JSON converter ==========
    @Bean Jackson2JsonMessageConverter messageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf, Jackson2JsonMessageConverter mc) {
        var f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(mc);
        return f;
    }
}
