package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.manager.RedisManager;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class SpikeService {
	@Autowired
	private CommonService commonService;

	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private RedisManager redisManager;
	/**
	 * 优化秒杀，将秒杀要用到的一些参数写入缓存，如果这些值能通过验证再写入数据库
	 */
	public void init() {
		List<Spike> spikes = spikeMapper.selectAll();
		int i = 0;//计数
		for (Spike spike : spikes){
			// 缓存秒杀验证数据
			redisManager.saveSpike(spike);
		}
	}

	public Result index(String order, int page){
		String o = commonService.orderFormat(order);
		int p = commonService.pageFormat(page);
		List<Spike> spikeList= spikeMapper.selectByPagination(o, (p - 1) * 10, 10);
		Integer count = spikeMapper.count();
		return new SpikeResult(SuccessCode.OK,spikeList, count);
	}

	/**
	 * 根据 id 查秒杀项目
	 * @param spikeId
	 * @return
	 */
	public Spike getSpikeById(Long spikeId){
		return spikeMapper.selectByPrimaryKey(spikeId);
	}


	public Result manage(User user, String order, int page) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		String o = commonService.orderFormat(order);
		int p = commonService.pageFormat(page);
		List<Spike> spikeList = spikeMapper.selectByUserIdPagination(user.getId(),o, (p - 1) * 10, 10);
		Integer count = spikeMapper.countByUserId();
		return new SpikeResult(SuccessCode.OK,spikeList, count);
	}

	public Result store(User user, SpikeVo spikeVo) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		Spike spike = new Spike();
		Date now = new Date();
		spike.setCreatedAt(now);
		spike.setUpdatedAt(now);
		spike.setUserId(user.getId());
		transformSpikeVoMessageToSpike(spikeVo,spike);
		spikeMapper.insertSelective(spike);
		redisManager.saveSpike(spike);
		ArrayList<Spike> arrayList = new ArrayList<>(1);
		arrayList.add(spike);
		return new SpikeResult(SuccessCode.OK,arrayList,1);
	}


	public Result show(User user,Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		List<Spike> list = new ArrayList<>(1);
		Spike spike = getSpikeById(id);// 从数据库中取出
		if (spike != null && spike.getUserId().equals(user.getId())){ // 校验取出的数据用户是否拥有
			list.add(spike);
			return new SpikeResult(SuccessCode.OK,list,1);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}

	public Result detail(Long id) {
		List<Spike> list = new ArrayList<>(1);
		Spike spike = getSpikeById(id);// 从数据库中取出
		if (spike != null){
			list.add(spike);
			return new SpikeResult(SuccessCode.OK,list,1);
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}

	public Result update(User user, SpikeVo spikeVo,Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		// 从数据库中根据传入的 spike id 取出
		Spike spikeInDB = getSpikeById(id);
		Date now = new Date();
		// 开始秒杀后禁止修改
		if (now.after(spikeInDB.getStartAt()) && now.before(spikeInDB.getEndAt())){
			return SpikeResult.TIME_NOT_ALLOW;
		}
		// 校验该 spike 用户是否拥有
		if (spikeInDB.getUserId().equals(user.getId())){
			// 如果拥有才能执行更新
			Spike spike = new Spike();
			spike.setId(id);
			spike.setUpdatedAt(new Date());
			transformSpikeVoMessageToSpike(spikeVo,spike);
			spikeMapper.updateByPrimaryKeySelective(spike);
			redisManager.saveSpike(spike);
			return SpikeResult.OK_WITHOUT_BODY;
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}

	public Result delete(User user,Long id){
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		Spike spike = getSpikeById(id);// 从数据库中取出
		if (spike != null && spike.getUserId().equals(user.getId())){ // 校验取出的数据用户是否拥有
			// 删除数据库
			spikeMapper.deleteByPrimaryKey(id);
			// 删除 redis
			redisManager.removeSpike(spike);
			return SpikeResult.OK_WITHOUT_BODY;
		}else{
			return AuthResult.AUTH_FAILED;
		}
	}

	private void transformSpikeVoMessageToSpike(SpikeVo spikeVo,Spike spike){
		spike.setName(spikeVo.getName());
		spike.setDetail(spikeVo.getDetail());
		spike.setStock(spikeVo.getStock());
		spike.setStartAt(spikeVo.getStartAt());
		spike.setEndAt(spikeVo.getEndAt());
		spike.setPrice(spikeVo.getPrice());
	}

}
