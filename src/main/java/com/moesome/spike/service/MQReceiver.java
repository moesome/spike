package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.SpikeOrderVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class MQReceiver {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@RabbitListener(queues = MQConfig.TOPIC_SPIKE_QUEUE)
	public void receiveSpikeTopic(SpikeOrderVo spikeOrderVo){
		spikeOrderService.ResolveOrder(spikeOrderVo);
	}

}
