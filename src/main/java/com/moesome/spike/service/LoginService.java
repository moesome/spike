package com.moesome.spike.service;

import com.moesome.spike.config.RedisConfig;
import com.moesome.spike.exception.exception.PassWordMismatchException;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.po.User;
import com.moesome.spike.model.vo.LoginResult;
import com.moesome.spike.model.vo.LoginVo;
import com.moesome.spike.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Service
public class LoginService {
	@Autowired
	UserMapper userMapper;

	@Autowired
	RedisTemplate<String,User> redisTemplate;

	public LoginResult login(LoginVo loginVo , HttpServletResponse httpServletResponse){
		String client = EncryptUtil.md5(loginVo.getPassword());
		User user = userMapper.selectByUsername(loginVo.getUsername());
		if (user == null){
			throw new PassWordMismatchException();
		}
		String db = user.getPassword();
		if (!client.equals(db)){
			throw new PassWordMismatchException();
		}
		String token = UUID.randomUUID().toString().replace("-","");
		redisTemplate.opsForValue().set(token, user);

		Cookie cookie = new Cookie("token",token);
		cookie.setMaxAge(RedisConfig.EXPIRE_SECOND);
		cookie.setPath("/");
		httpServletResponse.addCookie(cookie);
		return new LoginResult(200,user);
	}
}
