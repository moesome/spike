package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.MailVo;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MQReceiver {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@Autowired
	private SendService sendService;

	@RabbitListener(queues = MQConfig.SPIKE_QUEUE)
	public void receiveSpike(SpikeOrderVo spikeOrderVo){
		spikeOrderService.resolveOrder(spikeOrderVo);
	}

	@RabbitListener(queues = MQConfig.MAIL_QUEUE)
	public void receiveMail(MailVo mailVo){
		System.out.println("准备发送邮件");
		sendService.resolveSendMail(mailVo);
	}

}
