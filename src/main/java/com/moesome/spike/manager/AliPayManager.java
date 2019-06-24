package com.moesome.spike.manager;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moesome.spike.model.dao.RechargeMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Recharge;
import com.moesome.spike.model.pojo.vo.AliPayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class AliPayManager {
	@Autowired
	private AlipayClient alipayClient;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private RedisManager redisManager;

	@Autowired
	private RechargeMapper rechargeMapper;

	private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 创建订单并唤起支付页面
	 * @param httpServletResponse
	 * @param model
	 * @param aliPayVo
	 */
	public void pay(HttpServletResponse httpServletResponse, String model, AliPayVo aliPayVo){
		AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
		alipayRequest.setReturnUrl("https://api.moesome.com/"+model+"/return");
		alipayRequest.setNotifyUrl("https://api.moesome.com/"+model+"/notify");//在公共参数中设置回跳和通知地址
		try {
			alipayRequest.setBizContent(objectMapper.writeValueAsString(aliPayVo));//填充业务参数
			System.out.println(alipayRequest.getBizContent());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String form="";
		try {
			form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		httpServletResponse.setContentType("text/html;charset=UTF-8");
		try {
			httpServletResponse.getWriter().write(form);//直接将完整的表单html输出到页面
			httpServletResponse.getWriter().flush();
			httpServletResponse.getWriter().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 手动检测订单是否完成
	 * @param aliPayVo
	 */
	public boolean check(AliPayVo aliPayVo){
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		try {
			request.setBizContent(objectMapper.writeValueAsString(aliPayVo));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		AlipayTradeQueryResponse response = null;
		try {
			response = alipayClient.execute(request);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		if(response.isSuccess()){
			// 校验成功则处理订单
			return resolve(aliPayVo.getOutTradeNo(),response.getTotalAmount(),simpleDateFormat.format(response.getSendPayDate()),response.getTradeNo());
		} else {
			return false;
		}
	}

	public boolean resolve(String id, String amount, String payAt, String trade_no) {
		// 本地订单号加入缓存，避免重复处理
		if(!redisManager.saveRechargeId(Long.valueOf(id))){
			return false;
		}
		Recharge recharge = rechargeMapper.selectByPrimaryKey(Long.valueOf(id));
		if (recharge == null||recharge.getCoin().compareTo(new BigDecimal(amount)) != 0){
			// 请求错误
			return false;
		}
		if (recharge.getStatus()!=0){
			// 已处理订单，加入缓存
			redisManager.saveRechargeId(Long.valueOf(id));
		}
		recharge.setStatus((byte)1);

		try {
			recharge.setPayAt(simpleDateFormat.parse(payAt));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try{
			recharge.setTradeNo(trade_no);
		}catch (Exception e){
			e.printStackTrace();
		}
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				// 写订单
				rechargeMapper.updateByPrimaryKeySelective(recharge);
				// 给用户加金币
				userMapper.incrementCoinById(recharge.getCoin(),recharge.getUserId());
			}
		});
		return true;
	}
}
