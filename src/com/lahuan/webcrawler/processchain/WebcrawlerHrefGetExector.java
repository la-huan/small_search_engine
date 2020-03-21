package com.lahuan.webcrawler.processchain;

import java.util.List;

import com.lahuan.common.env.webcrawler.ProcessChainContext;
import com.lahuan.common.queue.SearchEngineQueue;
import com.lahuan.common.search.HTMLFetchUtil;

/**
 * 提取url 再放入url队列进行
 * @author la-huan
 *
 */
public class WebcrawlerHrefGetExector implements WebcrawlerChainExector {
	
	public WebcrawlerHrefGetExector(SearchEngineQueue queueLinks) {
		super();
		this.queueLinks = queueLinks;
	}

	SearchEngineQueue queueLinks;//利用生产者功能
	
	@Override
	public boolean exec(ProcessChainContext ctx) {
		String doc = ctx.getDoc();
		List<String> urls = HTMLFetchUtil.getHtmlAllHref(doc);
		for (String url : urls) {
			try {
				queueLinks.send(url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

}
