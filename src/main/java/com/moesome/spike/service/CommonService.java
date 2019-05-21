package com.moesome.spike.service;

import com.moesome.spike.config.RedisConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 存放基本公用操作
 */
@Service
public class CommonService {

	public String orderFormat(String order){
		if (StringUtils.isEmpty(order) || order.equals("ascend")){
			order = "ASC";
		}else{
			order = "DESC";
		}
		return order;
	}
	public int pageFormat(int page){
		if (page < 0){
			page = 1;
		}
		return page;
	}
	public void setCookie(String sessionId, HttpServletResponse httpServletResponse){
		if (httpServletResponse == null)
			return;
		Cookie cookie = new Cookie("sessionId",sessionId);
		cookie.setMaxAge(RedisConfig.EXPIRE_SECOND);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
	}
}
