package com.example.mailplatformalarmservice.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${SPRING_RABBITMQ_HOST}")
    private String rabbitmqHost;

    @Value("${SPRING_RABBITMQ_PORT}")
    private int rabbitmqPort;

    @Value("${SPRING_RABBITMQ_USERNAME}")
    private String rabbitmqUserName;

    @Value("${SPRING_RABBITMQ_PASSWORD}")
    private String rabbitmqPassword;

    @Value("${RABBITMQ_ALARM_1_QUEUE_NAME}")
    private String alarm1QueueName;

    @Value("${RABBITMQ_ALARM_1_ROUTING_KEY}")
    private String alarm1RoutingKey;

    @Value("${RABBITMQ_ALARM_2_QUEUE_NAME}")
    private String alarm2QueueName;

    @Value("${RABBITMQ_ALARM_2_ROUTING_KEY}")
    private String alarm2RoutingKey;

    @Value("${RABBITMQ_ALARM_3_QUEUE_NAME}")
    private String alarm3QueueName;

    @Value("${RABBITMQ_ALARM_3_ROUTING_KEY}")
    private String alarm3RoutingKey;

    @Value("${RABBITMQ_ALARM_EXCHANGE_NAME}")
    private String alarmExchangeName;

    @Bean
    public Queue alarm1Queue() {
        return new Queue(alarm1QueueName);
    }

    @Bean
    public Queue alarm2Queue() { return new Queue(alarm2QueueName); }

    @Bean
    public Queue alarm3Queue() {
        return new Queue(alarm3QueueName);
    }

    @Bean
    public TopicExchange alarmExchange() {
        return new TopicExchange(alarmExchangeName);
    }

    @Bean
    public Binding alarm1Binding(Queue alarm1Queue, TopicExchange alarmExchange) {
        return BindingBuilder.bind(alarm1Queue).to(alarmExchange).with(alarm1RoutingKey);
    }

    @Bean
    public Binding alarm2Binding(Queue alarm2Queue, TopicExchange alarmExchange) {
        return BindingBuilder.bind(alarm2Queue).to(alarmExchange).with(alarm2RoutingKey);
    }

    @Bean
    public Binding alarm3Binding(Queue alarm3Queue, TopicExchange alarmExchange) {
        return BindingBuilder.bind(alarm3Queue).to(alarmExchange).with(alarm3RoutingKey);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitmqHost);
        connectionFactory.setPort(rabbitmqPort);
        connectionFactory.setUsername(rabbitmqUserName);
        connectionFactory.setPassword(rabbitmqPassword);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}