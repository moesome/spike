package com.moesome.spike.model.pojo.result;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.ErrorCode;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.pojo.vo.SendVo;
import lombok.Data;

import java.util.List;

@Data
public class SendResult extends Result<List<SendVo>>{
	private int count;

	public static final SendResult NOTICE_SUCCESS = new SendResult(SuccessCode.OK);
	public static final SendResult WRONG_REQUEST = new SendResult(ErrorCode.REQUEST_ERR);

	public SendResult(Code code) {
		super(code);
	}

	public SendResult(Code code, List<SendVo> object,int count) {
		super(code, object);
		this.count = count;
	}
}
