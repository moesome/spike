package com.moesome.spike.model.vo.receive;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthVo {
	@NotNull
	private String username;

	@NotNull
	@Length(min = 32,max = 32)
	private String password;
}
