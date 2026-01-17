package com.lms.transaction.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TRANSACTION_EXCHANGE = "transaction.exchange";
    public static final String TRANSACTION_CREATED_QUEUE = "transaction.created.queue";
    public static final String TRANSACTION_CREATED_ROUTING_KEY = "transaction.created";
    public static final String POINTS_EARNED_EXCHANGE = "points.earned.exchange";
    public static final String POINTS_EARNED_QUEUE = "points.earned.queue";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public FanoutExchange pointsEarnedExchange() {
        return new FanoutExchange(POINTS_EARNED_EXCHANGE);
    }

    @Bean
    public Queue transactionCreatedQueue() {
        return new Queue(TRANSACTION_CREATED_QUEUE);
    }

    @Bean
    public Queue pointsEarnedQueue() {
        return new Queue(POINTS_EARNED_QUEUE);
    }

    @Bean
    public Binding transactionCreatedBinding(Queue transactionCreatedQueue, TopicExchange transactionExchange) {
        return BindingBuilder.bind(transactionCreatedQueue).to(transactionExchange)
                .with(TRANSACTION_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding pointsEarnedBinding(Queue pointsEarnedQueue, FanoutExchange pointsEarnedExchange) {
        return BindingBuilder.bind(pointsEarnedQueue).to(pointsEarnedExchange);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }

    @Bean
    public org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate = new org.springframework.amqp.rabbit.core.RabbitTemplate(
                connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
