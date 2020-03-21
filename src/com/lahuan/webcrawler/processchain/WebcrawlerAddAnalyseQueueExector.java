package com.lahuan.webcrawler.processchain;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.env.webcrawler.ProcessChainContext;
import com.lahuan.common.queue.ConcurrentFileQueue;
import com.lahuan.common.queue.SearchEngineQueue;

/**
 * 
 * 把相关内容放到分析队列
 * 
 * @author la-huan
 *
 */
public class WebcrawlerAddAnalyseQueueExector implements WebcrawlerChainExector {
	
	SearchEngineQueue queueAnalyse;// 利用生产者功能
	ConcurrentFileQueue queueUrlId;// url的ID队列
	QueueIdAdapter adapter;
	public WebcrawlerAddAnalyseQueueExector(SearchEngineQueue queueAnalyse,ConcurrentFileQueue queueUrlId) {
		super();
		this.queueAnalyse = queueAnalyse;
		this.queueUrlId = queueUrlId;
		adapter=new QueueIdAdapter(queueAnalyse, queueUrlId);
	}

	@Override
	public boolean exec(ProcessChainContext ctx) {

		try {
			// 分析队列
			String formatHtml = (String) ctx.get(GlobelConfig.ANALYSE_FORMAT_HTML_KEY);
			String url = ctx.getUrl();
			// URL的ID +URL+ 格式化后的文档信息
			adapter.write(url, formatHtml);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

}
