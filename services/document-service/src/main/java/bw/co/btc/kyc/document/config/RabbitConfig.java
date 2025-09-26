package bw.co.btc.kyc.document.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;           // <-- AMQP Queue
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;


@Configuration
public class RabbitConfig {
    @Bean TopicExchange ocrExchange(@Value("${app.ocr.exchange:x.ocr}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    Queue ocrQueue() { return QueueBuilder.durable("q.ocr.request").build(); }

    @Bean
    Binding ocrBinding(Queue ocrQueue, TopicExchange ocrExchange,
                       @Value("${app.ocr.routingKey:ocr.request}") String key) {
        return BindingBuilder.bind(ocrQueue).to(ocrExchange).with(key);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
