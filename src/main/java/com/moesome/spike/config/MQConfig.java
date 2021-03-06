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
	public static final String SPIKE_QUEUE = "spike";
	public static final String MAIL_QUEUE = "mail";
	public static final String DIRECT_EXCHANGE = "directExchange";
	public static final String DIRECT_SPIKE_EXCHANGE_ROUTING_KEY = "spikeOrder";
	public static final String DIRECT_MAIL_EXCHANGE_ROUTING_KEY = "mailOrder";

	// 设置对象转化器
	@Bean
	MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public Queue spikeQueue(){
		return new Queue(SPIKE_QUEUE,true);
	}
	@Bean
	public Queue mailQueue(){
		return new Queue(MAIL_QUEUE,true);
	}
	// 构造交换机
	@Bean
	public DirectExchange directExchange(){
		return new DirectExchange(DIRECT_EXCHANGE);
	}

	// 根据返回值来确定该类是绑定类
	@Bean
	public Binding spikeDirectBinding(){
		return BindingBuilder.bind(spikeQueue()).to(directExchange()).with(DIRECT_SPIKE_EXCHANGE_ROUTING_KEY);
	}

	@Bean
	public Binding mailDirectBinding(){
		return BindingBuilder.bind(mailQueue()).to(directExchange()).with(DIRECT_MAIL_EXCHANGE_ROUTING_KEY);
	}


}

