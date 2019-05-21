package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.dao.SpikeOrderMapper;
import com.moesome.spike.model.dao.UserMapper;
import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.result.SendResult;
import com.moesome.spike.model.pojo.vo.SendVo;
import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeOrderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
				spikeOrderToChangeStatus.setStatus((byte) 4);
				spikeOrderMapper.updateByPrimaryKeySelective(spikeOrderToChangeStatus);

				// 发送邮件通知作者发货（还未实现）
				// 邮件包含收获者邮箱，和发货确认链接
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
			spikeOrderToChangeStatus.setStatus((byte) 5);
			spikeOrderMapper.updateByPrimaryKeySelective(spikeOrderToChangeStatus);
			return SendResult.NOTICE_SUCCESS;
		}else{
			return AuthResult.UNAUTHORIZED;
		}
	}
}
