package com.moesome.spike.model.vo.result;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.model.vo.receive.SpikeOrderAndSpikeVo;
import lombok.Data;

import java.util.List;

@Data
public class SpikeOrderResult extends Result<List<SpikeOrderAndSpikeVo>>{

	int count;

	public SpikeOrderResult(Code code) {
		super(code);
	}

	public SpikeOrderResult(Code code, List<SpikeOrderAndSpikeVo> object,int count) {
		super(code, object);
		this.count = count;
	}
}
