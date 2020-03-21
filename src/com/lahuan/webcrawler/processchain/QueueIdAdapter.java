package com.lahuan.webcrawler.processchain;

import java.util.concurrent.locks.ReentrantLock;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.queue.ConcurrentFileQueue;
import com.lahuan.common.queue.SearchEngineQueue;

public class QueueIdAdapter {

	SearchEngineQueue queueAnalyse;// 分析队列
	ConcurrentFileQueue queueUrlId;// url的ID队列
	ReentrantLock lock = new ReentrantLock();// 锁

	public QueueIdAdapter(SearchEngineQueue queueAnalyse, ConcurrentFileQueue queueUrlId) {
		super();
		this.queueAnalyse = queueAnalyse;
		this.queueUrlId = queueUrlId;
	}

	/**
	 * 加锁填充ID 保证ID有序
	 * 
	 * @param url
	 * @param formatHtml
	 * @throws Exception
	 */
	public void write(String url, String formatHtml) throws Exception {
		lock.lock();
		long id = queueUrlId.write(url.getBytes()) - 1;//返回的是队列大小 减一即从0开始的队列
		// ID +URL + DOC
		String info = id + GlobelConfig.ANALYSE_QUEUE_SEPARATE + url + GlobelConfig.ANALYSE_QUEUE_SEPARATE + formatHtml;
		queueAnalyse.send(info);
		lock.unlock();
	}
}
