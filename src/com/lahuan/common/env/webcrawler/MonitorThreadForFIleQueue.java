package com.lahuan.common.env.webcrawler;

import com.lahuan.analyse.tempindex.TempIndexMap;
import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.queue.SearchEngineFileQueue;
import com.lahuan.common.util.SelfLogger;

/**
 * 监控线程 负责状态的切换
 * 
 * @author la-huan
 *
 */
public class MonitorThreadForFIleQueue extends Thread {
	private long thresholdAnalyse;// 分析队列阈值
	private SearchEngineFileQueue queueAnalyse;
	private SearchEngineFileQueue queueLinks;
	private TempIndexMap<Long, Long> tempIndexMap;

	public MonitorThreadForFIleQueue(long thresholdAnalyse, SearchEngineFileQueue queueAnalyse,
			SearchEngineFileQueue queueLinks, TempIndexMap<Long, Long> tempIndexMap) {
		super();
		this.thresholdAnalyse = thresholdAnalyse;
		this.queueAnalyse = queueAnalyse;
		this.queueLinks = queueLinks;
		this.tempIndexMap = tempIndexMap;
	}

	public void run() {
		firstCheck();
		while (true) {
			try {
				// 每次等待实际
				Thread.sleep(GlobelConfig.THREAD_CHECK_LOOP_PER_MS);
				boolean analyseFull = queueAnalyse.getQueue().getQueueSize() > thresholdAnalyse;
				if (analyseFull) {
					// 分析队列满
					ProcessGlobelContext.setStatus(ProcessRunStatus.persistence);
				}
				SelfLogger.log("监控信息:queueAnalyse size:" + queueAnalyse.getQueue().getQueueSize() + ";queueLinks size:"
						+ queueLinks.getQueue().getQueueSize() + ";tempIndexMap key:" + tempIndexMap.getKeySize()
						+ ",tempIndexMap value:" + tempIndexMap.getValueSize() + ".");
				if (ProcessGlobelContext.getStatus() == ProcessRunStatus.persistence) {
					// 开启持久化后就不再输出监控信息
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	// 检测是否开启
	private void firstCheck() {
		if (queueAnalyse.getQueue().getQueueSize() < thresholdAnalyse) {
			ProcessGlobelContext.setStatus(ProcessRunStatus.start);
		}
	}

}
