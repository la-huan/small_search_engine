package com.lahuan.analyse.thread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lahuan.analyse.tempindex.TempIndexMap;
import com.lahuan.common.env.webcrawler.ProcessRunStatus;
import com.lahuan.common.io.PersistenceIndex;
import com.lahuan.common.io.PersistenceOffset;
import com.lahuan.common.util.SelfLogger;

public class AnalysePersistenceThread extends Thread{
	//倒排索引
	TempIndexMap<Long, Long> tempIndexMap;
	//写入索引信息
	PersistenceIndex persistenceIdx;
	//写入索引偏移量
	PersistenceOffset persistenceOffset;

	public AnalysePersistenceThread(TempIndexMap<Long, Long> tempIndexMap, PersistenceIndex persistenceIdx,
			PersistenceOffset persistenceOffset) {
		super();
		this.tempIndexMap = tempIndexMap;
		this.persistenceIdx = persistenceIdx;
		this.persistenceOffset = persistenceOffset;
	}

	@Override
	public void run() {
		ProcessRunStatus.loopCheck(ProcessRunStatus.persistence);//检测状态
		SelfLogger.log("60秒后开始持久化操作.");
		try {
			Thread.sleep(1000*60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SelfLogger.log("持久化操作.开始");
		// 将map持久化到本地 并且持久化开始后 其他线程都必须关闭 否则会出现错误
		Map<Long, Collection<Long>> all = tempIndexMap.getAll();
		Set<Long> set = all.keySet();
		List<Long> list = new ArrayList<Long>(set);
		List<Long> offsets = new ArrayList<Long>();
		Collections.sort(list);
		for (Long id : list) {
			Collection<Long> collection = all.get(id);
			String line = id + collection.toString();
			long offset = persistenceIdx.write(line.getBytes());
			offsets.add(offset);
		}
		SelfLogger.log("持久化操作.索引写入完成");
		// 最后将offset也本地化到磁盘
		for (Long off : offsets) {
			persistenceOffset.write(off);
		}
		SelfLogger.log("结束持久化操作.偏移量写入完成.自动关闭..");
		//退出
		System.exit(0);
	}

}
