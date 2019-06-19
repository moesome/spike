package com.moesome.spike.model.pojo.vo;

import com.alibaba.druid.support.json.JSONParser;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class AliPayVo {
	/**
	 * 商户订单号,64个字符以内、可包含字母、数字、下划线；需保证在商户端不重复
 	 */
	@JsonProperty("out_trade_no")
	private String outTradeNo;

	/**
	 * 销售产品码，与支付宝签约的产品码名称。 注：目前仅支持 FAST_INSTANT_TRADE_PAY
	 */
	@JsonProperty("product_code")
	private final String productCode = "FAST_INSTANT_TRADE_PAY";

	/**
	 * 订单总金额，单位为元，精确到小数点后两位，取值范围[0.01,100000000]。
	 */
	@JsonProperty("total_amount")
	private Float totalAmount;

	/**
	 * 订单标题
	 */
	private String subject;

	/**
	 * 商品主类型 :0-虚拟类商品,1-实物类商品 注：虚拟类商品不支持使用花呗渠道
	 */
	@JsonProperty("goods_type")
	private final Integer goodsType = 0;

	@JsonProperty("timeout_express")
	private final String timeoutExpress = "30m";
}

