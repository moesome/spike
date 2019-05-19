package com.moesome.spike.service;

import com.moesome.spike.model.domain.SpikeOrder;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeOrderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SendService {
	@Autowired
	private SpikeService spikeService;

	@Autowired
	private SpikeOrderService spikeOrderService;

	public Result remindToSendProduction(User user, Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		SpikeOrder spikeOrder = spikeOrderService.selectByPrimaryKey(id);
		if (spikeOrder.getUserId().equals(user.getId())){
			Long spikeId = spikeOrder.getSpikeId();
			// 根据 spikeId 查创建者，先查出订单（后续考虑增加发送邮件功能）
			// 然后根据订单中的 id 编号查询，这里使用联表查询。
			SpikeAndUserContactWayVo spikeAndUserContactWayBySpikeId = spikeService.getSpikeAndUserContactWayBySpikeId(spikeId);
			// 改变订单状态
			SpikeOrder spikeOrderToChangeStatus = new SpikeOrder();
			spikeOrderToChangeStatus.setId(id);
			spikeOrderToChangeStatus.setStatus((byte) 4);
			spikeOrderService.updateByPrimaryKeySelective(spikeOrderToChangeStatus);

			// 发送邮件通知作者发货
			// 邮件包含收获者邮箱，和发货确认链接

			return SpikeOrderResult.NOTICE_SUCCESS;
		}else {
			return AuthResult.UNAUTHORIZED;
		}
	}

	public Result index(int page, String order, User user) {
		String o = CommonService.orderFormat(order);
		int p = CommonService.pageFormat(page);
		/*List<Spike> spikeList = spikeMapper.selectByPagination(o, (p - 1) * 10, 10);
		int count = spikeMapper.count();
		return new SpikeResult(SuccessCode.OK,spikeList, count);*/
		return null;
	}
}
