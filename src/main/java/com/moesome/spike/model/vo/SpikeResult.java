package com.moesome.spike.model.vo;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.model.domain.Spike;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class SpikeResult extends Result<List<Spike>>{
	int count;

	public SpikeResult(Code code) {
		super(code);
	}

	public SpikeResult(Code code, List<Spike> spikeList,int count) {
		super(code, spikeList);
		this.count = count;
	}
}
