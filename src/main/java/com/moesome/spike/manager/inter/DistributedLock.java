package com.moesome.spike.manager.inter;

public interface DistributedLock {
	/**
	 * 轮询，直到拿到锁位置
	 * @param Id
	 */
	void lockSpike(Long Id);

	/**
	 * 直接加锁，没有拿到锁则返回 false
	 * @param Id
	 * @return
	 */
	Boolean tryLock(Long Id);

	/**
	 * 解锁
	 * @param Id
	 */
	void unlockSpike(Long Id);
}
