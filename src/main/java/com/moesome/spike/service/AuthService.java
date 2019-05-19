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
	private UserMapper userMapper;

	@Autowired
	private RedisTemplate<String,User> redisTemplate;

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
		String sessionId = saveUserAndGenerateSessionId(user);
		// 返回 cookie
		setCookie(sessionId,httpServletResponse);
		return new AuthResult(SuccessCode.OK,user);
	}

	public Result check(String sessionId, HttpServletResponse httpServletResponse) {
		if (StringUtils.isEmpty(sessionId)){
			return AuthResult.AUTH_FAILED;
		}
		User user = getUserBySessionId(sessionId);
		if (user == null){
			return AuthResult.AUTH_FAILED;
		}
		refreshMsgInRedis(sessionId,RedisConfig.EXPIRE_SECOND);
		setCookie(sessionId,httpServletResponse);
		return new AuthResult(SuccessCode.OK,user);
	}

	/**
	 * redis 根据 sessionId 读出 User
	 * @param sessionId
	 * @return
	 */
	public User getUserBySessionId(String sessionId){
		return redisTemplate.opsForValue().get(sessionId);
	}

	/**
	 * 将用户存入 redis
	 * @param user
	 * @return
	 */
	public String saveUserAndGenerateSessionId(User user){
		String sessionId = UUID.randomUUID().toString().replace("-","");
		redisTemplate.opsForValue().set(sessionId, user,RedisConfig.EXPIRE_SECOND,TimeUnit.SECONDS);
		return sessionId;
	}

	public void setCookie(String sessionId, HttpServletResponse httpServletResponse){
		if (httpServletResponse == null)
			return;
		Cookie cookie = new Cookie("sessionId",sessionId);
		cookie.setMaxAge(RedisConfig.EXPIRE_SECOND);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
	}

	/**
	 * 刷新 redis session 的缓存存在时间
	 * @param sessionId
	 */
	public void refreshMsgInRedis(String sessionId, int time){
		redisTemplate.expire(sessionId,time, TimeUnit.SECONDS);
	}

	public Result logout(String sessionId, HttpServletResponse httpServletResponse) {
		if (!StringUtils.isEmpty(sessionId)){
			setCookie("",httpServletResponse);
			refreshMsgInRedis(sessionId,0);
			return new AuthResult(SuccessCode.OK);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}
}
