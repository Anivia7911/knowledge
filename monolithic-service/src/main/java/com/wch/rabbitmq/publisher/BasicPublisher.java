package com.wch.rabbitmq.publisher;

import com.wch.rabbitmq.config.RabbitMqConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnBean(RabbitTemplate.class)
public class BasicPublisher {

    private RabbitTemplate rabbitTemplate;
    private RabbitMqConfig config;

    @Autowired
    void setService(
            RabbitTemplate rabbitTemplate,
            RabbitMqConfig config
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
    }

    public void sendBasicMsg(String msg) {

        try {
            if (msg != null && !msg.isEmpty()) {
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.setExchange(config.getBasicExchangeName());
                rabbitTemplate.setRoutingKey(config.getBasicRoutingKey());
                Message message = MessageBuilder.withBody(msg.getBytes()).build();
                rabbitTemplate.convertAndSend(message);
            }
        } catch (Exception e) {
            log.error("sendBasicMsg-生产者-发送消息异常:{}", e.getMessage(), e.fillInStackTrace());
        }
    }
}
