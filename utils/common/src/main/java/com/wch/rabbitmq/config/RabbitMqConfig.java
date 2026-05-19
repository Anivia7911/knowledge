package com.wch.rabbitmq.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Slf4j
@Data
@Configuration
@ConfigurationProperties("com.wch.rabbit-mq")
public class RabbitMqConfig {
    @Value("${basic.queue.name:}")
    private String basicQueueName;

    @Value("${basic.exchange.name:}")
    private String basicExchangeName;

    @Value("${basic.routing.key:}")
    private String basicRoutingKey;

    private final CachingConnectionFactory connectionFactory;
    private final SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    @Autowired
    public RabbitMqConfig(
            SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer,
            CachingConnectionFactory connectionFactory
    ) {
        this.factoryConfigurer = factoryConfigurer;
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory multiRabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory, connectionFactory);
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(10);
        factory.setMaxConcurrentConsumers(15);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        connectionFactory.setPublisherReturns(true);

        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){
            @Override
            public void confirm(@Nullable CorrelationData correlationData, boolean b, @Nullable String s) {
                log.info("消息发送成功：correlationData({}),ack({}),cause({})", correlationData, b, s);
            }
        });
        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback(){
            @Override
            public void returnedMessage(ReturnedMessage returnedMessage) {
                log.info("消息丢失：returnedMessage({})", returnedMessage);
            }
        });
        return rabbitTemplate;
    }

    @Bean(name = "basicQueue")
    public Queue basicQueue() {
        return new Queue(basicQueueName, true);
    }

    @Bean
    public DirectExchange basicExchange() {
        return new DirectExchange(basicExchangeName, true, false);
    }

    @Bean
    public Binding basicBinding() {
        return BindingBuilder
                .bind(basicQueue())
                .to(basicExchange())
                .with(basicRoutingKey);
    }
}
