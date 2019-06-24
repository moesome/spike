package com.moesome.spike.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.moesome.spike.config.AliPayConfig;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@CrossOrigin("*")
@RestController
public class RechargeController {
	@Autowired
	private RechargeService rechargeService;

	@GetMapping("recharge/{coin}")
	public Result recharge(HttpServletResponse httpServletResponse, User user,@PathVariable  BigDecimal coin){
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		if (coin.compareTo(BigDecimal.ZERO) <= 0)
			return Result.REQUEST_ERR;
		return rechargeService.recharge(httpServletResponse,user,coin);
	}

	/**
	 * 接用户回调，该回调即使不执行也没什么问题，定时任务会处理订单
	 * @param msg
	 * @param httpServletResponse
	 */
	@GetMapping("recharge/return")
	public void returnURL(@RequestParam Map<String, String> msg,HttpServletResponse httpServletResponse){
		boolean signVerified = false; //调用SDK验证签名
		try {
			signVerified = AlipaySignature.rsaCheckV1(msg, AliPayConfig.ALIPAY_PUBLIC_KEY, AliPayConfig.CHARSET, AliPayConfig.SIGN_TYPE);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		System.out.println("Return:"+msg);
		if(signVerified){
			// 验签
			String appId = msg.get("auth_app_id");
			if (appId.equals(AliPayConfig.APP_ID)){
				rechargeService.check(msg.get("out_trade_no"));
			}else{
				System.out.println("error");
			}
		}else{
			System.out.println("验签失败");
		}
		try {
			httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL("http://spike.moesome.com"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 接支付宝回调，该回调即使不执行也没什么问题，定时任务会处理订单
	 * @param msg
	 */
	@PostMapping("recharge/notify")
	public void notifyURL(@RequestParam Map<String, String> msg){
		System.out.println("收到 notify");
		boolean signVerified = false; //调用SDK验证签名
		try {
			signVerified = AlipaySignature.rsaCheckV1(msg, AliPayConfig.ALIPAY_PUBLIC_KEY, AliPayConfig.CHARSET, AliPayConfig.SIGN_TYPE);
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}
		if(signVerified){
			// 状态 TRADE_SUCCESS 成功过
			String status = msg.get("trade_status");
			// 验证
			String appId = msg.get("auth_app_id");
			if (status.equals("TRADE_SUCCESS")&&appId.equals(AliPayConfig.APP_ID)){
				System.out.println("进入 resolver");
				String id = msg.get("out_trade_no");
				String payAt = msg.get("gmt_payment");
				String trade_no = msg.get("trade_no");
				String amount = msg.get("total_amount");
				rechargeService.resolve(id,amount,payAt,trade_no);
			}else{
				System.out.println("error");
			}
		}else{
			System.out.println("验签失败");
		}
		System.out.println("notify"+msg);
	}
}
