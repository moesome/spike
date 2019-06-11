package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.MailVo;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class MQSender {
	@Autowired
	AmqpTemplate amqpTemplate;

	@Autowired
	ThreadPoolTaskExecutor threadPoolTaskExecutor;

	public void sendToSpikeTopic(SpikeOrderVo spikeOrderVo){
		// System.out.println("发送订单至队列"+spikeOrderVo);
		amqpTemplate.convertAndSend(MQConfig.DIRECT_EXCHANGE,MQConfig.DIRECT_SPIKE_EXCHANGE_ROUTING_KEY,spikeOrderVo);
	}

	public void sendToEmailTopic(MailVo mailVo) {
		amqpTemplate.convertAndSend(MQConfig.DIRECT_EXCHANGE, MQConfig.DIRECT_MAIL_EXCHANGE_ROUTING_KEY, mailVo);
	}

}
