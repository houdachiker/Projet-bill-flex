package com.billing.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ cote CONSUMER. Declaration identique a celle du publisher (service-facturation) :
 * declarer deux fois la meme ressource est idempotent, et la queue existe meme si le consumer demarre en dernier.
 *
 *   billing.events (topic) --[invoice.emise]--> notification.invoice.queue --> InvoiceEventListener
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "billing.events";
    public static final String QUEUE = "notification.invoice.queue";
    public static final String ROUTING_KEY = "invoice.emise";

    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue invoiceQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding invoiceBinding(Queue invoiceQueue, TopicExchange billingExchange) {
        return BindingBuilder.bind(invoiceQueue).to(billingExchange).with(ROUTING_KEY);
    }

    /** Messages en JSON (et non en serialisation Java) : lisibles dans la console RabbitMQ. */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
