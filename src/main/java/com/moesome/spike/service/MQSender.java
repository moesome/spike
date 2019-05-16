package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.domain.User;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
	@Autowired
	AmqpTemplate amqpTemplate;

	public void send(User msg){
		amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
	}

	public void sendToTopic(String msg){
		// 标志位 （topic.key1） 匹配到则入队列，本例中 key1 key2 都能入队列2，但只有 key1 能入队列1
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key1",msg+"to 1");
		amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.key2",msg+"to 2");
	}


	public void senderHeader(Object message){
		MessageProperties messageProperties = new MessageProperties();
		messageProperties.setHeader("header1","value1");
		messageProperties.setHeader("header2","value2");
		Message obj = new Message("messages".getBytes(),messageProperties);
		amqpTemplate.convertAndSend(MQConfig.HEADS_EXCHANGE,"",obj);
	}
}
