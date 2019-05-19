package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.UserVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.UserResult;
import com.moesome.spike.util.EncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class UserService {
	@Autowired
	private UserMapper userMapper;

	@Autowired AuthService authService;

	public Result store(UserVo userVo){
		Long i = userMapper.selectIdByUsername(userVo.getUsername());
		if (i != null)
			return UserResult.USER_DUPLICATE;
		User user = new User();
		transformUserVoToUser(userVo,user);
		Date date = new Date();
		user.setCreatedAt(date);
		user.setUpdatedAt(date);
		userMapper.insert(user);
		return UserResult.OK_WITHOUT_BODY;
	}
	// 密码在这里加密
	private void transformUserVoToUser(UserVo userVo, User user){
		user.setUsername(userVo.getUsername());
		user.setNickname(userVo.getNickname());
		if (!userVo.getPassword().equals("")){
			user.setPassword(EncryptUtil.md5(userVo.getPassword()));
		}else{
			user.setPassword(null);
		}
		user.setEmail(userVo.getEmail());
		user.setPhone(userVo.getPhone());
	}

	public Result update(String sessionId, User user, UserVo userVo, Long id, HttpServletResponse httpServletResponse) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		// 判断传入用户是否和缓存中相等
		if (user.getId().equals(id)){
			transformUserVoToUser(userVo, user);
			user.setUpdatedAt(new Date());
			userMapper.updateByPrimaryKeySelective(user);
			// 删除旧缓存
			authService.refreshMsgInRedis(sessionId,0);
			// 创建新缓存
			String s = authService.saveUserAndGenerateSessionId(user);
			// 设置新 cookie
			authService.setCookie(s,httpServletResponse);
			return new UserResult(SuccessCode.OK,user);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}


	public Result show(User user, Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		// 判断传入用户是否和缓存中相等
		if (user.getId().equals(id)) {
			return new UserResult(SuccessCode.OK,user);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}
}
