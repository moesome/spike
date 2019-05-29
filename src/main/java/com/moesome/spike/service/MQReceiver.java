package com.moesome.spike.service;

import com.moesome.spike.config.MQConfig;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MQReceiver {
	@Autowired
	private SpikeOrderService spikeOrderService;

	@RabbitListener(queues = MQConfig.SPIKE_QUEUE)
	public void receiveSpikeTopic(SpikeOrderVo spikeOrderVo){
		spikeOrderService.resolveOrder(spikeOrderVo);
	}

}
