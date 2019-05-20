package com.moesome.spike.service;

import com.moesome.spike.exception.message.SuccessCode;
import com.moesome.spike.model.dao.SpikeMapper;
import com.moesome.spike.model.domain.Spike;
import com.moesome.spike.model.domain.User;
import com.moesome.spike.model.pojo.vo.SpikeAndUserContactWayVo;
import com.moesome.spike.model.pojo.vo.SpikeVo;
import com.moesome.spike.model.pojo.result.AuthResult;
import com.moesome.spike.model.pojo.result.Result;
import com.moesome.spike.model.pojo.result.SpikeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class SpikeService {
	@Autowired
	private SpikeMapper spikeMapper;

	@Autowired
	private RedisTemplate<String,Object> redisTemplateForSaveSpike;

	@Autowired
	private RedisTemplate<String,List<Spike>> redisTemplateForFirstPage;
	/**
	 * 优化秒杀，将秒杀要用到的一些参数写入缓存，如果这些值能通过验证再写入数据库
	 */
	public void init() {
		List<Spike> spikes = spikeMapper.selectAll();
		LinkedList<Spike> firstPage = new LinkedList<>();
		int i = 0;//计数
		for (Spike spike : spikes){
			// 缓存秒杀验证数据
			saveSpikeToRedis(spike);
			if (i + 10 >= spikes.size()){
				firstPage.push(spike);
			}
			i++;
		}
		// 缓存商品第一页
		redisTemplateForFirstPage.opsForValue().set("firstSpikePage",firstPage);
		// 缓存总数
		redisTemplateForSaveSpike.opsForValue().set("count",i);
	}

	public Result index(String order, int page){
		String o = CommonService.orderFormat(order);
		int p = CommonService.pageFormat(page);
		List<Spike> spikeList;
		Integer count;
		if (o.equals("DESC") && p == 1){
			// 返回缓存
			spikeList = redisTemplateForFirstPage.opsForValue().get("firstSpikePage");
			count = (Integer) redisTemplateForSaveSpike.opsForValue().get("count");
			// 缓存有效直接返回
			if (spikeList != null && count != null){
				// System.out.println("查缓存第一页");
				return new SpikeResult(SuccessCode.OK,spikeList, count);
			}
		}
		// 缓存无效或没有查询第一页则返回数据库内容
		spikeList = spikeMapper.selectByPagination(o, (p - 1) * 10, 10);
		count = spikeMapper.count();
		return new SpikeResult(SuccessCode.OK,spikeList, count);
	}

	public void reCacheFirstPage(){
		// System.out.println("刷新商品第一页缓存");
		// 缓存商品第一页
		redisTemplateForFirstPage.opsForValue().set("firstSpikePage",spikeMapper.selectByPagination("DESC", 0, 10));
		// 缓存总数
		redisTemplateForSaveSpike.opsForValue().set("count",spikeMapper.count());
	}

	/**
	 * 根据 id 查秒杀项目
	 * @param spikeId
	 * @return
	 */
	public Spike getSpikeById(Long spikeId){
		return spikeMapper.selectByPrimaryKey(spikeId);
	}

	public boolean decrementStock(Long spikeId){
		return spikeMapper.decrementStockById(spikeId) > 0;
	}

	private void saveSpikeToRedis(Spike spike){
		redisTemplateForSaveSpike.opsForHash().put("spike"+spike.getId(),"stock",spike.getStock());
		redisTemplateForSaveSpike.opsForHash().put("spike"+spike.getId(),"startAt",spike.getStartAt());
		redisTemplateForSaveSpike.opsForHash().put("spike"+spike.getId(),"endAt",spike.getEndAt());
	}

	public Result manage(User user, String order, int page) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		String o = CommonService.orderFormat(order);
		int p = CommonService.pageFormat(page);
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
		saveSpikeToRedis(spike);
		reCacheFirstPage();
		return SpikeResult.OK_WITHOUT_BODY;
	}

	public Result show(User user,Long id) {
		if (user == null)
			return AuthResult.UNAUTHORIZED;
		List<Spike> list = new ArrayList<>(1);
		Spike spike = getSpikeById(id);// 从数据库中取出
		if (spike.getUserId().equals(user.getId())){ // 校验取出的数据用户是否拥有
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
		// 校验该 spike 用户是否拥有
		if (spikeInDB.getUserId().equals(user.getId())){
			// 如果拥有才能执行更新
			Spike spike = new Spike();
			spike.setId(id);
			spike.setUpdatedAt(new Date());
			transformSpikeVoMessageToSpike(spikeVo,spike);
			spikeMapper.updateByPrimaryKeySelective(spike);
			saveSpikeToRedis(spike);
			reCacheFirstPage();
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
	}

	public SpikeAndUserContactWayVo getSpikeAndUserContactWayBySpikeId(Long spikeId) {
		return spikeMapper.selectSpikeAndUserContactWayBySpikeId(spikeId);
	}

}
