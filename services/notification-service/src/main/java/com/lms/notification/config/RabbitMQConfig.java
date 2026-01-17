package com.lms.notification.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String POINTS_EXCHANGE = "points.earned.exchange";
    public static final String NOTIFICATION_POINTS_QUEUE = "notification.points.queue";

    @Bean
    public org.springframework.amqp.core.FanoutExchange pointsExchange() {
        return new org.springframework.amqp.core.FanoutExchange(POINTS_EXCHANGE);
    }

    @Bean
    public org.springframework.amqp.core.Queue notificationPointsQueue() {
        return new org.springframework.amqp.core.Queue(NOTIFICATION_POINTS_QUEUE);
    }

    @Bean
    public org.springframework.amqp.core.Binding binding(org.springframework.amqp.core.Queue notificationPointsQueue,
            org.springframework.amqp.core.FanoutExchange pointsExchange) {
        return org.springframework.amqp.core.BindingBuilder.bind(notificationPointsQueue).to(pointsExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
