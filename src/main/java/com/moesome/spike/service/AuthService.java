package com.moesome.spike.service;

import com.moesome.spike.config.RedisConfig;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.vo.AuthVo;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
	@Autowired
	private CommonService commonService;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisService redisService;

	public Result login(AuthVo authVo, HttpServletResponse httpServletResponse){
		String client = EncryptUtil.md5(authVo.getPassword());
		User user = userMapper.selectByUsername(authVo.getUsername());
		if (user == null){
			return AuthResult.USERNAME_OR_PASSWORD_ERR;
		}
		String db = user.getPassword();
		if (!client.equals(db)){
			return AuthResult.USERNAME_OR_PASSWORD_ERR;
		}
		// 存入 redis
		String sessionId = redisService.saveUserAndGenerateSessionId(user);
		// 返回 cookie
		commonService.setCookie(sessionId,httpServletResponse);
		return new AuthResult(SuccessCode.OK,user);
	}

	public Result check(String sessionId, HttpServletResponse httpServletResponse) {
		if (StringUtils.isEmpty(sessionId)){
			return AuthResult.AUTH_FAILED;
		}
		User user = redisService.getUserBySessionId(sessionId);
		if (user == null){
			return AuthResult.AUTH_FAILED;
		}
		redisService.refreshMsgInRedis(sessionId,RedisConfig.EXPIRE_SECOND);
		commonService.setCookie(sessionId,httpServletResponse);
		return new AuthResult(SuccessCode.OK,user);
	}

	public Result logout(String sessionId, HttpServletResponse httpServletResponse) {
		if (!StringUtils.isEmpty(sessionId)){
			commonService.setCookie("",httpServletResponse);
			redisService.refreshMsgInRedis(sessionId,0);
			return new AuthResult(SuccessCode.OK);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}
}
