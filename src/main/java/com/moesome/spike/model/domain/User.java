package com.moesome.spike.model.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class User {

    private Integer id;

    private String username;

    /**
    * 两次 md5 第一次在客户端（防劫持），第二次在服务器（防数据泄露后被彩虹表破解）
    */
    @JsonIgnore
    private String password;

    private String nickname;

    /**
    * 创建时间
    */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createdAt;

    /**
    * 上次修改时间
    */
    @JsonIgnore
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date updatedAt;

    private String email;

    private String phone;
}