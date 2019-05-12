package com.moesome.spike.util;


import org.springframework.util.DigestUtils;

import java.util.Random;

public class EncryptUtil {
	public static String md5(String msg){
		return DigestUtils.md5DigestAsHex(msg.getBytes());
	}
}
