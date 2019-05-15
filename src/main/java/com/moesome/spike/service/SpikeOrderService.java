package com.moesome.spike.service;

import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.vo.OrderResult;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;

@Service
public class SpikeOrderService {

	@Autowired
	private SpikeService spikeService;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Transactional
	public OrderResult createOrder(User user,Long spickId){
		// 检查 id 是否传过来了
		if (spickId == null)
			return OrderResult.REQUEST_ERR;
		// 检查是否登录
		if (user == null)
			return OrderResult.UNAUTHORIZED;
		// 查库存
		Spike spike = spikeService.getSpikeById(spickId);
		// 判断是否大于 0

		// 使用了用户 id 和秒杀项目 id 的唯一索引，无需校验是否已经抢过，直接下订单，下单失败则说明已经抢过
		// 下订单
		SpikeOrder spikeOrder = new SpikeOrder(null, user.getId(), spike.getId(), new Date(), (byte) 1);
		int insert = spikeOrderMapper.insert(spikeOrder);
		System.out.println("inset="+insert);
		// 减库存，有并发问题
		spikeService.decrementStock(spickId);
		return new OrderResult(SuccessCode.OK);
	}
}
