package com.lms.member.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String POINTS_EXCHANGE = "points.earned.exchange";
    public static final String MEMBER_POINTS_QUEUE = "member.points.queue";

    @Bean
    public FanoutExchange pointsExchange() {
        return new FanoutExchange(POINTS_EXCHANGE);
    }

    @Bean
    public Queue memberPointsQueue() {
        return new Queue(MEMBER_POINTS_QUEUE);
    }

    @Bean
    public Binding binding(Queue memberPointsQueue, FanoutExchange pointsExchange) {
        return BindingBuilder.bind(memberPointsQueue).to(pointsExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
