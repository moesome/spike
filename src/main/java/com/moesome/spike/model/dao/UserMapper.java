package com.moesome.spike.model.dao;

import com.moesome.spike.model.domain.User;

public interface UserMapper {
    int insert(User record);
    int insertSelective(User record);
    User selectByUsername(String username);
}