package com.lahuan.analyse.thread;

import java.util.List;

import com.lahuan.analyse.processchain.AnalyseChainExector;
import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.env.webcrawler.ProcessChainContext;
import com.lahuan.common.env.webcrawler.ProcessRunStatus;
import com.lahuan.common.queue.SearchEngineQueue;
import com.lahuan.common.util.SelfLogger;

public class AnalyseIndexThread extends Thread{

	SearchEngineQueue queueAnalyse;// 利用消费者功能

	List<AnalyseChainExector> chainsAnalyse;
	
	
	
	public AnalyseIndexThread(SearchEngineQueue queueAnalyse, List<AnalyseChainExector> chainsAnalyse) {
		super();
		this.queueAnalyse = queueAnalyse;
		this.chainsAnalyse = chainsAnalyse;
	}



	@Override
	public void run() {
		while (true) {
			ProcessRunStatus.loopCheck(ProcessRunStatus.start);//检测状态
			//获取信息
			List<String> res = queueAnalyse.get();
			for (String r : res) {
				ProcessRunStatus.loopCheck(ProcessRunStatus.start);//检测状态
				String[] rs = r.split(GlobelConfig.ANALYSE_QUEUE_SEPARATE);
				if (rs.length != 3)
					continue;// 非法
				//构建上下文
				ProcessChainContext ctx = new ProcessChainContext(rs[1], rs[2], Long.valueOf(rs[0]));
				SelfLogger.log("分析url_id:"+rs[0]+",URL:"+rs[1]);
				for (AnalyseChainExector exector : chainsAnalyse) {
					//调用链
					if (!exector.exec(ctx)) {
						break;
					}
				}
			}
			try {
				// sleep(10)保证能让出线程 避免过度抢占线程 导致cpu占用暴增
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
