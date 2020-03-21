package com.lahuan.common.queue;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 同步文件队列
 * 在com.lahuan.common.queue.FileQueue的基础上加上可重入锁
 * @author la-huan
 *
 */
public class ConcurrentFileQueue {
	
	/**
	 * 调用的还是文件队列
	 */
	private FileQueue queue = null;
	/**
	 * 写锁 读写是分开的 可以分别设置锁
	 */
	private ReentrantLock writeLock = new ReentrantLock();
	/**
	 * 读锁 读写是分开的 可以分别设置锁
	 */
	private ReentrantLock readLock = new ReentrantLock();

	/**
	 * 磁盘路径 队列名
	 * 
	 * @param fileDir
	 * @param queueName
	 */
	public ConcurrentFileQueue(String fileDir, String queueName) {
		queue = new FileQueue(fileDir, queueName);
	}

	

	/**
	 * 读指针重置 可以重新顺序读取
	 * 
	 * @throws Exception
	 */
	public void rewind() throws Exception {
		readLock.lock();
		queue.rewind();
		readLock.unlock();
	}

	/**
	 * 顺序读取 自动记录读指针
	 * 
	 * @return
	 * @throws Exception
	 */
	public byte[] seqGet() throws Exception {
		readLock.lock();
		byte[] bs = queue.seqGet();
		readLock.unlock();
		return bs;
	}

	/**
	 * 随机读指定位置的数据  每次都新建文件映射
	 * 随机读不加锁
	 * @param idx
	 * @return
	 * @throws Exception
	 */
	public byte[] randomGet(long idx) throws Exception {
		return queue.randomGet(idx);
	}

	
	/**
	 * 写入
	 * 
	 * @param b
	 * @return 
	 * @throws Exception
	 */
	public long write(byte[] b) throws Exception {
		writeLock.lock();
		long size =queue.write(b);
		writeLock.unlock();
		return size;
	}
	/**
	 * 清空队列<br>
	 * 这里只是重设指针,没有真正的删除文件
	 */
	public void clear() {
		writeLock.lock();
		readLock.lock();
		queue.clear();
		writeLock.unlock();
		readLock.unlock();
	}

	/**
	 * 获取未消费的数据数量
	 * 
	 * @return
	 */
	public long getLeftNum() {
		return queue.getLeftNum();
	}

	/**
	 * 队列总大小 包含已消费的
	 * 
	 * @return
	 */
	public long getQueueSize() {
		return queue.getQueueSize();
	}

	/**
	 * 获取顺序读指针的位置
	 * 
	 * @return
	 */
	public long getReadIdx() {
		return queue.getReadIdx();
	}

	/**
	 * 获取顺序写指针的位置
	 * 
	 * @return
	 */
	public long getWriteIdx() {
		return queue.getWriteIdx();
	}

	/**
	 * 获取索引指针的位置
	 * 
	 * @return
	 */
	public long getIndexIdx() {
		return queue.getIndexIdx();
	}

}
