package com.moesome.spike.model.dao;

import com.moesome.spike.model.po.User;
import org.apache.ibatis.annotations.Mapper;

public interface UserMapper {
    int insert(User record);
    int insertSelective(User record);
    User selectByUsername(String username);
}