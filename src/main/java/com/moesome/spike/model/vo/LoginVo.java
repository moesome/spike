package com.moesome.spike.model.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
public class LoginVo {
	@NotNull
	private String username;

	@NotNull
	@Length(min = 32,max = 32)
	private String password;
}
