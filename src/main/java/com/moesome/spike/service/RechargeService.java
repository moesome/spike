package com.moesome.spike.service;

import com.moesome.spike.manager.AliPayManager;
import com.moesome.spike.manager.RedisManager;
import com.moesome.spike.model.dao.RechargeMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Recharge;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.AliPayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Service
public class RechargeService {
	@Autowired
	private AliPayManager aliPayManager;

	@Autowired
	private RechargeMapper rechargeMapper;


	public Result recharge(HttpServletResponse httpServletResponse, User user, BigDecimal coin) {
		// 创建订单
		Recharge recharge = new Recharge();
		recharge.setCreatedAt(new Date());
		recharge.setStatus((byte)0);
		// 暂时只有支付宝
		recharge.setWay((byte)1);
		recharge.setCoin(coin);
		recharge.setUserId(user.getId());
		rechargeMapper.insertSelective(recharge);

		AliPayVo aliPayVo = new AliPayVo();
		aliPayVo.setOutTradeNo(recharge.getId().toString());
		aliPayVo.setSubject("用户充值");
		aliPayVo.setTotalAmount(coin.floatValue());

		aliPayManager.pay(httpServletResponse,"recharge",aliPayVo);
		// 在 pay 里页面被重定向到支付页面
		return null;
	}
	// 仅做 controller 中转
	public void resolve(String id, String amount, String payAt, String trade_no) {
		aliPayManager.resolve(id,amount,payAt,trade_no);
	}

	/**
	 * 向支付宝发起校验请求，校验成功则处理订单
	 * @param id
	 * @return
	 */
	public boolean check(String id) {
		AliPayVo aliPayVo = new AliPayVo();
		aliPayVo.setOutTradeNo(id);
		return aliPayManager.check(aliPayVo);
	}

	@Scheduled(initialDelay=30*1000,fixedRate = 3*60*1000)
	public void scanAndResolveRecharge(){
		Date now = new Date(System.currentTimeMillis() - 40*60*1000);
		List<Recharge> recharges = rechargeMapper.selectAllUnResolver();
		recharges.stream()
				.filter(
						// 将所有的订单 check 一次，若 check 失败了 and 订单创建时间到现在已经超过 40 分钟
						// 收集这些订单进行下一步处理
						((Predicate<Recharge>) recharge -> !check(recharge.getId().toString()))
						.and(recharge -> recharge.getCreatedAt().before(now))
				)
				// 遍历过期且未支付订单，对其进行标记
				.forEach(recharge -> {
					recharge.setStatus((byte)-1);
					rechargeMapper.updateByPrimaryKeySelective(recharge);
				});
	}
}
