package com.moesome.spike.model.pojo.result;

import com.moesome.spike.exception.message.Code;
import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.pojo.vo.SpikeOrderAndSpikeVo;
import lombok.Data;

import java.util.List;

@Data
public class SpikeOrderResult extends Result<List<SpikeOrderAndSpikeVo>>{

	Integer count;

	public SpikeOrderResult(Code code) {
		super(code);
	}

	public SpikeOrderResult(Code code, List<SpikeOrderAndSpikeVo> object,Integer count) {
		super(code, object);
		this.count = count;
	}
}
