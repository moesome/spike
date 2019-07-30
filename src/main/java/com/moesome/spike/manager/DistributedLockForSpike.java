package com.moesome.spike.manager;

import com.moesome.spike.manager.inter.DistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DistributedLockForSpike implements DistributedLock {
	@Autowired
	private RedisManager redisManager;

	@Override
	public void lockSpike(Long spikeId) {
		while (true){
			if(tryLock(spikeId)){
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Boolean tryLock(Long spikeId) {
		return redisManager.saveSpikeIdIfAbsent(spikeId);
	}

	@Override
	public void unlockSpike(Long spikeId) {
		redisManager.deleteSpikeId(spikeId);
	}
}
