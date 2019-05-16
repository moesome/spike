package com.moesome.spike.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moesome.spike.exception.message.Code;
import com.moesome.spike.model.domain.Spike;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SpikeResult extends Result<List<Spike>>{
	int count;
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	Date now;

	public SpikeResult(Code code) {
		super(code);
	}

	public SpikeResult(Code code, List<Spike> spikeList,int count) {
		super(code, spikeList);
		this.count = count;
		this.now = new Date();
	}
}
