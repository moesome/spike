package com.moesome.spike;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moesome.spike.controller.*;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeResult;
import com.moesome.spike.model.pojo.vo.*;
import com.moesome.spike.manager.AliPayManager;
import com.moesome.spike.util.EncryptUtil;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.Cookie;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpikeApplicationTest {
	@Autowired
	private UserController userController;

	@Autowired
	private AuthController authController;

	private  MockHttpServletResponse mockHttpServletResponse;

	private User user;

	private String sessionId;

	@Autowired
	private SpikeController spikeController;

	@Autowired
	private SpikeOrderController spikeOrderController;

	@Autowired
	private SendController sendController;

	// 以下三个仅用于输出调试信息
	@Autowired
	private UserMapper userMapper;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private AliPayManager aliPayManager;

	@Before
	public void before(){
		mockHttpServletResponse = new MockHttpServletResponse();
		// 创建用户
		UserVo userVo = new UserVo();
		userVo.setUsername("test"+Math.random());
		userVo.setEmail("1053770594@qq.com");
		userVo.setNickname("testNick");
		userVo.setPassword(EncryptUtil.md5("1"));
		userVo.setPhone("12345678963");
		Result store = userController.store(userVo);
		Assert.assertEquals("创建用户错误", 0, (long)store.getCode());
		// 登录
		AuthVo authVo = new AuthVo();
		authVo.setUsername(userVo.getUsername());
		authVo.setPassword(EncryptUtil.md5("1"));
		Result result = authController.login(authVo, mockHttpServletResponse);
		Assert.assertEquals("登录失败", 0, (long)result.getCode());
		// 校验登录
		sessionId = mockHttpServletResponse.getCookie("sessionId").getValue();
		Result check = authController.check(sessionId, mockHttpServletResponse);
		Assert.assertEquals("检查登录失败", 0, (long)check.getCode());
		// 存储用户
		user = (User)check.getObject();
	}

	@Test
	public void spikeTest(){
		// 创建 spike
		Spike spike = storeSpike(false);
		// 修改 spike
		updateSpike(spike);
		// 查询 spike
		Spike spikeShow = showSpike(spike.getId());
		Assert.assertEquals("商品更新测试失败", (Integer) 3, spikeShow.getStock());
		// 删除 spike
		deleteSpike(spike.getId());
	}

	@Test
	public void spikeOrderTest(){
		// 创建 spike
		Spike spike = storeSpike(true);
		// 新增 order
		storeSpikeOrder(spike);
		// 循环 check order
		checkSpikeOrder(spike);
		// 查询 order
		SpikeOrderAndSpikeVo spikeOrderAndSpikeVo = showSpikeOrder(spike);
		// 删除 order
		deleteSpikeOrder(spikeOrderAndSpikeVo);
		// 删除 spike
		deleteSpike(spike.getId());
	}

	@Test
	public void sendTest(){
		// 创建 spike
		Spike spike = storeSpike(true);
		// 新增 order
		storeSpikeOrder(spike);
		// 循环 check order
		checkSpikeOrder(spike);
		// 查询 order
		SpikeOrderAndSpikeVo spikeOrderAndSpikeVo = showSpikeOrder(spike);
		// 查询所有订单，取第一个
		Result index = sendController.index(1, "descend", user);
		Assert.assertEquals("查询订单失败", (Integer)0, index.getCode());
		List<SendVo> sendVos = (List<SendVo>) index.getObject();
		SendVo sendVo = sendVos.get(0);
		printMessage(user.getId(),spikeOrderAndSpikeVo.getSpikeId(),spikeOrderAndSpikeVo.getSpikeOrderId(),"初始化");
		// 提醒发货
		Result result = sendController.remindToSendProduction(user, sendVo.getSpikeOrderId());
		Assert.assertEquals("提醒发货失败", (Integer)0, result.getCode());
		printMessage(user.getId(),spikeOrderAndSpikeVo.getSpikeId(),spikeOrderAndSpikeVo.getSpikeOrderId(),"提醒发货");
		try {
			// 等发邮件
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 发货
		Result result1 = sendController.sendProduction(user, sendVo.getSpikeOrderId());
		Assert.assertEquals("发货失败", (Integer)0, result1.getCode());
		printMessage(user.getId(),spikeOrderAndSpikeVo.getSpikeId(),spikeOrderAndSpikeVo.getSpikeOrderId(),"发货");
		// 收货
		Result result2 = sendController.receivedProduction(user, sendVo.getSpikeOrderId());
		Assert.assertEquals("收货失败", (Integer)0, result2.getCode());
		printMessage(user.getId(),spikeOrderAndSpikeVo.getSpikeId(),spikeOrderAndSpikeVo.getSpikeOrderId(),"收货");
		// 删除 order
		deleteSpikeOrder(spikeOrderAndSpikeVo);
		// 删除 spike
		deleteSpike(spike.getId());
		printMessage(user.getId(),spikeOrderAndSpikeVo.getSpikeId(),spikeOrderAndSpikeVo.getSpikeOrderId(),"删除 spike 、order");
	}

	/**
	 * 输出数据库中可能变动的信息
	 */
	private void printMessage(Long userId,Long spikeId,Long spikeOrderId,String msg){
		User user = userMapper.selectByPrimaryKey(userId);
		Spike spike = spikeMapper.selectByPrimaryKey(spikeId);
		SpikeOrder spikeOrder = spikeOrderMapper.selectByPrimaryKey(spikeOrderId);
		System.out.println(msg);
		System.out.println(user);
		System.out.println(spike);
		System.out.println(spikeOrder);
		System.out.println("========");
	}

	private void storeSpikeOrder(Spike spike){
		SpikeOrderVo spikeOrderVo = new SpikeOrderVo();
		spikeOrderVo.setSpikeId(spike.getId());
		Result store = spikeOrderController.store(sessionId, user, spikeOrderVo);
		Assert.assertEquals("下订单失败", (Integer)0, store.getCode());
	}

	private void checkSpikeOrder(Spike spike){
		Result check = spikeOrderController.check(user, spike.getId());
		System.out.println("check :"+check);
		while(check.getCode() != 0&&check.getCode() != -513){
			check = spikeOrderController.check(user, spike.getId());
			System.out.println("check :"+check);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals("检查订单失败", (Integer)0, check.getCode());
	}

	private SpikeOrderAndSpikeVo showSpikeOrder(Spike spike){
		Result index = spikeOrderController.index(user, 1, "descend");
		List<SpikeOrderAndSpikeVo> object = (List<SpikeOrderAndSpikeVo>) index.getObject();
		SpikeOrderAndSpikeVo spikeOrderAndSpikeVo = object.get(0);
		Assert.assertEquals("查询订单失败", spike.getId(), spikeOrderAndSpikeVo.getSpikeId());
		Assert.assertEquals("查询订单失败", user.getId(), spikeOrderAndSpikeVo.getUserId());
		return spikeOrderAndSpikeVo;
	}

	private void deleteSpikeOrder(SpikeOrderAndSpikeVo spikeOrderAndSpikeVo){
		Result delete = spikeOrderController.delete(user, spikeOrderAndSpikeVo.getSpikeOrderId(),spikeOrderAndSpikeVo);
		Assert.assertEquals("删除订单失败", (Integer)0, delete.getCode());
	}



	private Spike storeSpike(boolean isStart){
		SpikeVo spikeVo = new SpikeVo();
		spikeVo.setStock(2);
		spikeVo.setPrice(BigDecimal.ZERO);
		spikeVo.setEndAt(new Date(System.currentTimeMillis()+100000));
		if (isStart){
			spikeVo.setStartAt(new Date(System.currentTimeMillis()-100000));
		}else{
			spikeVo.setStartAt(new Date(System.currentTimeMillis()+100000));
		}
		spikeVo.setDetail("test");
		spikeVo.setName("test" + Math.random());
		Result result = spikeController.store(user, spikeVo);
		Assert.assertEquals("创建商品失败", 0, (long)result.getCode());
		List<Spike> spikes = ((SpikeResult) result).getObject();
		return spikes.get(0);
	}

	private void updateSpike(Spike spike){
		SpikeVo spikeVo = new SpikeVo();
		spikeVo.setStock(3);
		spikeVo.setEndAt(new Date(System.currentTimeMillis()+100000));
		spikeVo.setStartAt(new Date(System.currentTimeMillis()-100000));
		spikeVo.setDetail("test");
		spikeVo.setName("test"+Math.random());
		Result result = spikeController.update(user, spike.getId(), spikeVo);
		Assert.assertEquals("商品更新失败", 0, (long)result.getCode());
	}

	private Spike showSpike(Long spikeId){
		Result result = spikeController.show(user, spikeId);
		Assert.assertEquals("商品查询测试失败", 0, (long)result.getCode());
		List<Spike> spikes3 = ((SpikeResult) result).getObject();
		return spikes3.get(0);
	}

	private void deleteSpike(Long spikeId){
		Result delete = spikeController.delete(user, spikeId);
		Assert.assertEquals("商品删除测试失败", 0, (long)delete.getCode());
	}



	@After
	public void after(){
		// 退出登录
		String sessionId;
		for (Cookie cookie : mockHttpServletResponse.getCookies()){
			sessionId = cookie.getValue();
			authController.logout(sessionId, mockHttpServletResponse);
		}
		// 删除用户
		userController.delete("",user.getId());
	}


}
