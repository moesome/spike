package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.domain.User;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {
	// 监听队列，收到消息时候调用
	@RabbitListener(queues = MQConfig.QUEUE)
	public void receive(User msg){
		System.out.println("direct queue:"+msg);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
	public void receiveTopic(String msg){
		System.out.println("topic1 queue:"+msg);
	}

	@RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
	public void receiveTopic2(String msg){
		System.out.println("topic2 queue:"+msg);
	}

	@RabbitListener(queues = MQConfig.HEADER_QUEUE)
	public void receiveHeader(String ms){
		System.out.println("topic2 queue:"+ms);
	}
}
