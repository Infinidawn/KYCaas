// config/RabbitConfig.java
package bw.co.btc.kyc.risk.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // -------- consume from document-service --------
    @Bean
    public TopicExchange documentExchange() {
        return ExchangeBuilder.topicExchange("x.document").durable(true).build();
    }

    @Bean
    public Queue ocrCompletedQueue() {
        // Risk-engine's local queue to receive document.ocr.completed
        return QueueBuilder.durable("q.risk.document.ocr.completed").build();
    }

    @Bean
    public Binding ocrCompletedBinding(Queue ocrCompletedQueue, TopicExchange documentExchange) {
        return BindingBuilder.bind(ocrCompletedQueue)
                .to(documentExchange)
                .with("document.ocr.completed");
    }

// -------- consume from biometric-service --------

    @Bean public TopicExchange biometricExchange() {
        return ExchangeBuilder.topicExchange("x.biometric").durable(true).build();
    }
    @Bean public Queue biometricCompletedQueue() {
        return QueueBuilder.durable("q.risk.biometric.completed").build();
    }
    @Bean public Binding biometricCompletedBinding(Queue biometricCompletedQueue, TopicExchange biometricExchange) {
        return BindingBuilder.bind(biometricCompletedQueue).to(biometricExchange).with("biometric.completed");
    }

    // -------- produce to onboarding via risk exchange --------
    @Bean
    public TopicExchange riskExchange() {
        return ExchangeBuilder.topicExchange("x.risk").durable(true).build();
    }

    // -------- JSON converter + template --------
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        var tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(mc);
        return tpl;
    }
}
