package com.moesome.spike;

import com.moesome.spike.controller.SpikeController;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeResult;
import com.moesome.spike.model.pojo.vo.SpikeVo;
import com.moesome.spike.service.CommonService;
import com.moesome.spike.service.SpikeService;
import com.moesome.spike.util.EncryptUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SpikeControllerTest {
	@Autowired
	private SpikeController spikeController;

	@Autowired
	private TestCommon testCommon;

	private User user;

	private Spike spike;

	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

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
		Result result = spikeController.index(1, "descend");
		Assert.assertEquals("索引失败", 0, (long)result.getCode());
		Result result2 = spikeController.index(1, "ascend");
		Assert.assertEquals("索引失败", 0, (long)result2.getCode());
	}

	@Test
	public void manage(){
		Result result = spikeController.manage(user, 1, "descend");
		Assert.assertEquals("manage 测试失败", 0, (long)result.getCode());
	}

	@Test
	public void show(){
		Result result = spikeController.show(user, spike.getId());
		Assert.assertEquals("show 测试失败", 0, (long)result.getCode());
	}

	@Test
	public void store(){
		SpikeVo spikeVo = new SpikeVo();
		spikeVo.setStock(2);
		spikeVo.setEndAt(new Date(System.currentTimeMillis()+100000));
		spikeVo.setStartAt(new Date());
		spikeVo.setDetail("test");
		spikeVo.setName("test" + Math.random());
		Result result = spikeController.store(user, spikeVo);
		Assert.assertEquals("store 测试失败", 0, (long)result.getCode());
		List<Spike> spikes = ((SpikeResult) result).getObject();
		Spike spike = spikes.get(0);
		Result delete = spikeController.delete(user, spike.getId());
		Assert.assertEquals("删除商品错误", 0, (int)delete.getCode());
	}

	@Test
	public void update(){
		SpikeVo spikeVo = new SpikeVo();
		spikeVo.setStock(2);
		spikeVo.setEndAt(new Date(System.currentTimeMillis()+100000));
		spikeVo.setStartAt(new Date());
		spikeVo.setDetail("test");
		spikeVo.setName("test"+Math.random());
		Result result = spikeController.update(user, spike.getId(), spikeVo);
		// 无需额外删除缓存，after 里已经删了
		Assert.assertEquals("update 测试失败", 0, (long)result.getCode());
	}
}
