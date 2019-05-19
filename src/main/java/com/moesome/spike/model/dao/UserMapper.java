package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SendVo;

import java.util.List;

public interface UserMapper {
	int deleteByPrimaryKey(Long id);

	int insert(User record);

	int insertSelective(User record);

	User selectByPrimaryKey(Long id);

	int updateByPrimaryKeySelective(User record);

	int updateByPrimaryKey(User record);

	Long selectIdByUsername(String username);

	User selectByUsername(String username);

	List<SendVo> selectSendVoByUserId(Long id);
}