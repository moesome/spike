package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.SendResult;
import com.moesome.spike.model.pojo.vo.MailVo;
import com.moesome.spike.model.pojo.vo.SendVo;
import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeOrderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SendService {
	@Autowired
	private CommonService commonService;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private SpikeOrderMapper spikeOrderMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private MQSender mqSender;

	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private TransactionTemplate transactionTemplate;

	public Result remindToSendProduction(User user, Long spikeOrderId) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		SpikeOrder spikeOrder = spikeOrderMapper.selectByPrimaryKey(spikeOrderId);
		if (spikeOrder.getUserId().equals(user.getId())){
			Long spikeId = spikeOrder.getSpikeId();
			// 根据 spikeId 查创建者，先查出订单（后续考虑增加发送邮件功能）
			// 然后根据订单中的 id 编号查询，这里使用联表查询。
			SpikeAndUserContactWayVo spikeAndUserContactWayBySpikeId = spikeMapper.selectSpikeAndUserContactWayBySpikeId(spikeId);
			if (spikeOrder.getStatus() == 1){
				// 改变订单状态
				SpikeOrder spikeOrderToChangeStatus = new SpikeOrder();
				spikeOrderToChangeStatus.setId(spikeOrderId);
				spikeOrderToChangeStatus.setStatus((byte) 2);
				spikeOrderMapper.updateByPrimaryKeySelective(spikeOrderToChangeStatus);
				// 发送邮件通知作者发货
				MailVo mailVo = new MailVo();
				mailVo.setTitle("发货提醒");
				mailVo.setTo(spikeAndUserContactWayBySpikeId.getEmail());
				mailVo.setMsg("用户"+user.getUsername()+"提醒您及时发送礼物");
				mqSender.sendToEmailTopic(mailVo);
				return SendResult.NOTICE_SUCCESS;
			}else{
				return SendResult.WRONG_REQUEST;
			}
		}else {
			return AuthResult.UNAUTHORIZED;
		}
	}

	public Result index(int page, String order, User user) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		String o = commonService.orderFormat(order);
		int p = commonService.pageFormat(page);
		List<SendVo> sendVos = userMapper.selectSendVoByUserId(user.getId(), o, (p - 1) * 10, 10);
		// System.out.println(sendVos);
		Integer count = userMapper.countSendVoByUserId(user.getId());
		return new SendResult(SuccessCode.OK,sendVos,count);
	}

	public Result sendProduction(User user, Long spikeOrderId) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		Long spikeOrderOwnerId = spikeOrderMapper.selectSpikeOwnerIdBySpikeOrderId(spikeOrderId);
		if (spikeOrderOwnerId.equals(user.getId())){
			SpikeOrder spikeOrderToChangeStatus = new SpikeOrder();
			spikeOrderToChangeStatus.setId(spikeOrderId);
			spikeOrderToChangeStatus.setStatus((byte) 3);
			spikeOrderMapper.updateByPrimaryKeySelective(spikeOrderToChangeStatus);
			return SendResult.NOTICE_SUCCESS;
		}else{
			return AuthResult.UNAUTHORIZED;
		}
	}

	public Result receivedProduction(User user, Long spikeOrderId){
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		Long spikeOrderOwnerId = spikeOrderMapper.selectSpikeOwnerIdBySpikeOrderId(spikeOrderId);
		if (spikeOrderOwnerId.equals(user.getId())){
			SpikeOrder spikeOrderToChangeStatus = new SpikeOrder();
			spikeOrderToChangeStatus.setId(spikeOrderId);
			spikeOrderToChangeStatus.setStatus((byte) 4);
			try{
				transactionTemplate.execute(new TransactionCallbackWithoutResult() {
					@Override
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						// 更新订单状态
						spikeOrderMapper.updateByPrimaryKeySelective(spikeOrderToChangeStatus);
						// 查询订单金币
						BigDecimal price = spikeOrderMapper.selectPriceByPrimaryKey(spikeOrderId);
						// 增加创建者金币
						userMapper.incrementCoinById(price,user.getId());
					}
				});
			}catch (Exception e){
				return SendResult.NOTICE_ERROR;
			}
			return SendResult.NOTICE_SUCCESS;
		}else{
			return AuthResult.UNAUTHORIZED;
		}
	}



	public void sendMail(MailVo mailVo){
		mqSender.sendToEmailTopic(mailVo);
	}

	// 发送邮件操作耗时，扔给线程池处理，避免占用了过多的消费者数使得其他消费者无法得到执行
	@Async
	void resolveSendMail(MailVo mailVo){
		System.out.println("处理邮件");
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("contact@mail.moesome.com");
		message.setTo(mailVo.getTo());
		message.setSubject(mailVo.getTitle());
		message.setText(mailVo.getMsg());
		javaMailSender.send(message);
	}
}
