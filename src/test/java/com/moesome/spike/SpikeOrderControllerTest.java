package com.moesome.spike;

import com.moesome.spike.controller.SpikeOrderController;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import com.moesome.spike.service.RedisService;
import com.moesome.spike.service.SpikeOrderService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpikeOrderControllerTest {
	@Autowired
	private TestCommon testCommon;

	private User user;

	private Spike spike;

	@Autowired
	private SpikeOrderController spikeOrderController;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

	@Autowired
	private RedisTemplate<String, SpikeOrderVo> redisTemplateForSpikeOrderVo;

	@Autowired
	private RedisTemplate<String, SpikeOrder> redisTemplateForSpikeOrder;

	@Before
	public void createUserAndSpike(){
		System.out.println("create");
		user = testCommon.createUser();
		spike = testCommon.createSpike(2,new Date(),new Date(System.currentTimeMillis()+10000));
		redisTemplate.opsForHash().put("spike"+spike.getId(),"stock",spike.getStock());
		redisTemplate.opsForHash().put("spike"+spike.getId(),"startAt",spike.getStartAt());
		redisTemplate.opsForHash().put("spike"+spike.getId(),"endAt",spike.getEndAt());
	}

	@After
	public void deleteUser(){
		System.out.println("delete");
		testCommon.deleteUser();
		testCommon.deleteSpike();
		redisTemplate.opsForHash().delete("spike"+spike.getId(),"stock","startAt","endAt");
	}

	@Test
	public void index(){
		Result result = spikeOrderController.index(user, 1, "descend");
		Assert.assertEquals("index 错误", 0, (long)result.getCode());
	}

	@Test
	public void storeAndCheck(){
		SpikeOrderVo spikeOrderVo = new SpikeOrderVo();
		spikeOrderVo.setSpikeId(spike.getId());
		spikeOrderVo.setUserId(user.getId());
		Result store = spikeOrderController.store("",user, spikeOrderVo);
		System.out.println("store :"+store);
		Result check = spikeOrderController.check(user, spike.getId());
		while(check.getCode() != 0){
			check = spikeOrderController.check(user, spike.getId());
			System.out.println("check :"+check);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("check :"+check);
		// 清理
		spikeOrderMapper.deleteByUserIdAndSpikeId(user.getId(),spike.getId());
		redisTemplateForSpikeOrderVo.opsForSet().remove("spike_order_vo", spikeOrderVo);
		redisTemplateForSpikeOrder.opsForValue().set("spikeOrder-userId:"+spikeOrderVo.getUserId()+"-spikeId:"+spikeOrderVo.getSpikeId(), RedisService.ORDER_FAILED,1, TimeUnit.SECONDS);
	}

	@Test
	public void test(){
		ArrayList<Object> list = new ArrayList<>(3);
		list.add("startAt");
		list.add("endAt");
		list.add("price");
		List<Object> multiGet = redisTemplate.opsForHash().multiGet("spike" + 1,list);
		Date startAt = (Date)multiGet.get(0);
		Date endAt = (Date)multiGet.get(1);
		BigDecimal price = (BigDecimal)multiGet.get(2);
		System.out.println(startAt);
		System.out.println(endAt);
		System.out.println(price);
	}
}
