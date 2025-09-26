package bw.co.btc.kyc.document.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public TopicExchange ocrExchange(@Value("${app.ocr.exchange}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }
}
