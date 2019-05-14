package com.moesome.spike.model.vo;

import com.moesome.spike.model.domain.Spike;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SpikeResult {
	List<Spike> spikeList;
	int count;
}
