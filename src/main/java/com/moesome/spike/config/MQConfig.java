package com.moesome.spike.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Configuration
public class MQConfig {
	public static final String TOPIC_SPIKE_QUEUE = "topicSpike";
	public static final String TOPIC_SPIKE_QUEUE_EXCHANGE = "topicExchange";
	public static final String TOPIC_SPIKE_QUEUE_ROUTING_KEY = "topicSpikeRoutingKey";

	// 设置对象转化器
	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public Queue topicSpikeQueue(){
		return new Queue(TOPIC_SPIKE_QUEUE,true);
	}

	// 构造交换机
	@Bean
	public TopicExchange topicExchange(){
		return new TopicExchange(TOPIC_SPIKE_QUEUE_EXCHANGE);
	}

	// 根据返回值来确定该类是绑定类
	@Bean
	public Binding topicBinding(){
		// 发送时路由信息为 topic.spike 才会被转发到 topicSpikeQueue 队列
		return BindingBuilder.bind(topicSpikeQueue()).to(topicExchange()).with(TOPIC_SPIKE_QUEUE_ROUTING_KEY);
	}
}

