package com.moesome.spike.service;

import org.springframework.util.StringUtils;

public class CommonService {
	public static String orderFormat(String order){
		if (StringUtils.isEmpty(order) || order.equals("ascend")){
			order = "ASC";
		}else{
			order = "DESC";
		}
		return order;
	}
	public static int pageFormat(int page){
		if (page < 0){
			page = 1;
		}
		return page;
	}
}
