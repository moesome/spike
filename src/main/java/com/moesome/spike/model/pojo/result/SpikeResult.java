package com.moesome.spike.model.pojo.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.domain.Spike;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SpikeResult extends Result<List<Spike>>{
	Integer count;

	Date now;
	public static final SpikeResult OK_WITHOUT_BODY = new SpikeResult(SuccessCode.OK);

	public SpikeResult(Code code) {
		super(code);
	}

	public SpikeResult(Code code, List<Spike> spikeList,Integer count) {
		super(code, spikeList);
		this.count = count;
		this.now = new Date();
	}
}
