package com.moesome.spike;

import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.service.SpikeOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpikeApplicationTests {

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Test
	public void contextLoads() {
		SpikeOrder spikeOrder = new SpikeOrder();
		spikeOrder.setSpikeId(1L);
		spikeOrder.setUserId(1);
		int insert = spikeOrderMapper.insert(spikeOrder);
		System.out.println(insert);
	}

}
