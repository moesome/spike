package com.moesome.spike;

import com.moesome.spike.controller.SendController;
import com.moesome.spike.controller.SpikeOrderController;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.vo.MailVo;
import com.moesome.spike.model.pojo.vo.SpikeOrderAndSpikeVo;
import com.moesome.spike.model.pojo.vo.SpikeOrderVo;
import com.moesome.spike.service.MQSender;
import com.moesome.spike.service.RedisService;
import com.moesome.spike.service.SendService;
import com.moesome.spike.service.SpikeOrderService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SendControllerTest {

	@Autowired
	private RedisTemplate<String,Object> redisTemplate;

	@Autowired
	private TestCommon testCommon;

	@Autowired
	private SpikeOrderController spikeOrderController;

	private User user;

	private Spike spike;

	@Autowired
	private RedisTemplate<String, SpikeOrderVo> redisTemplateForSpikeOrderVo;

	@Autowired
	private RedisTemplate<String, SpikeOrder> redisTemplateForSpikeOrder;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	private SpikeOrderVo spikeOrderVo;

	@Autowired
	private SendController sendController;

	@Before
	public void createUser(){
		// 创建用户
		user = testCommon.createUser();
		// 创建商品
		spike = testCommon.createSpike(2,new Date(System.currentTimeMillis()-1000000),new Date(System.currentTimeMillis()+1000000));
		// 缓存商品
		redisTemplate.opsForHash().put("spike"+spike.getId(),"stock",spike.getStock());
		redisTemplate.opsForHash().put("spike"+spike.getId(),"startAt",spike.getStartAt());
		redisTemplate.opsForHash().put("spike"+spike.getId(),"endAt",spike.getEndAt());
		redisTemplate.opsForHash().put("spike"+spike.getId(),"price",spike.getPrice());
		// 下单
		spikeOrderVo = new SpikeOrderVo();
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
	}

	@After
	public void deleteUser(){
		testCommon.deleteUser();
		testCommon.deleteSpike();
		redisTemplate.opsForHash().delete("spike"+spike.getId(),"stock","startAt","endAt");
		// 清理
		spikeOrderMapper.deleteByUserIdAndSpikeId(user.getId(),spike.getId());
		redisTemplateForSpikeOrderVo.opsForSet().remove("spike_order_vo", spikeOrderVo);
		redisTemplateForSpikeOrder.opsForValue().set("spikeOrder-userId:"+spikeOrderVo.getUserId()+"-spikeId:"+spikeOrderVo.getSpikeId(), RedisService.ORDER_FAILED,1, TimeUnit.SECONDS);
	}

	@Test
	public void remindToSendProduction(){
		Result result = spikeOrderController.index(user, 1, "desend");
		List<SpikeOrderAndSpikeVo> resultObject = (List<SpikeOrderAndSpikeVo>) result.getObject();
		SpikeOrderAndSpikeVo spikeOrderAndSpikeVo = resultObject.get(0);
		Long spikeOrderId = spikeOrderAndSpikeVo.getSpikeOrderId();
		Result result1 = sendController.remindToSendProduction(user, spikeOrderId);
		Assert.assertEquals("remindToSendProduction 测试失败", 0, (long)result1.getCode());
		try {
			// 等待邮件发送
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void sendProduction(){
		Result result = spikeOrderController.index(user, 1, "desend");
		List<SpikeOrderAndSpikeVo> resultObject = (List<SpikeOrderAndSpikeVo>) result.getObject();
		SpikeOrderAndSpikeVo spikeOrderAndSpikeVo = resultObject.get(0);
		Long spikeOrderId = spikeOrderAndSpikeVo.getSpikeOrderId();
		Result result1 = sendController.sendProduction(user, spikeOrderId);
		Assert.assertEquals("sendProduction 测试失败", 0, (long)result1.getCode());
	}

	/*@Autowired
	MQSender mqSender;

	@Test
	public void test(){
		for (int i = 0;i<100;i++){
			mqSender.test();
		}
		try {
			Thread.sleep(999999);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}*/
}
