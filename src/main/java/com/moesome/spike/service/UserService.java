package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.manager.RedisManager;
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
	private CommonService commonService;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RedisManager redisManager;

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
		User returnUser = new User();
		returnUser.setId(user.getId());
		return new UserResult(SuccessCode.OK,returnUser);
	}
	// 密码在这一步之后已经加密
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

	public Result update(String sessionId, User user, UserVo userVo, Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		// 判断传入用户是否和缓存中相等
		if (user.getId().equals(id)){
			transformUserVoToUser(userVo, user);
			user.setUpdatedAt(new Date());
			userMapper.updateByPrimaryKeySelective(user);
			// 删除旧缓存
			redisManager.saveUser(user,sessionId);
			// 刷新第一页（第一页用户名可能会变）
			redisManager.reCacheFirstPage();
			return new UserResult(SuccessCode.OK,user);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}


	public Result show(User user,String sessionId) {
		User userInDB = userMapper.selectByPrimaryKey(user.getId());
		if (userInDB.equals(user)){
			return new UserResult(SuccessCode.OK,user);
		}else{
			// 刷新缓存
			redisManager.saveUser(userInDB,sessionId);
			return new UserResult(SuccessCode.OK,userInDB);
		}

	}

	public Result delete(String sessionId,Long id){
		// 校验 sessionId 是否为管理员（该功能为了测试方便暂时没有加）
		userMapper.deleteByPrimaryKey(id);
		redisManager.refreshUser(sessionId,0);
		return UserResult.OK_WITHOUT_BODY;
	}

	public User getUserByUserName(String username){
		return userMapper.selectByUsername(username);
	}
}
