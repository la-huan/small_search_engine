package com.lahuan.analyse.processchain;

import java.util.LinkedList;
import java.util.List;

import com.lahuan.analyse.tempindex.TempIndexMap;
import com.lahuan.common.env.webcrawler.ProcessChainContext;
import com.lahuan.common.search.ACAutomaton;
import com.lahuan.common.search.ACAutomatonResult;
import com.lahuan.common.util.SelfLogger;

/**
 * 构建索引
 * 
 * @author la-huan
 *
 */

public class AnalyseBuildTempIndexChainExector implements AnalyseChainExector {
	ACAutomaton acAllWords;// 全部词库的ac自动机

	TempIndexMap<Long, Long> tempIndexMap;// 存放倒排索引的结果

	public AnalyseBuildTempIndexChainExector(ACAutomaton acAllWords, TempIndexMap<Long, Long> tempIndexMap) {
		super();
		this.acAllWords = acAllWords;
		this.tempIndexMap = tempIndexMap;
	}

	@Override
	public boolean exec(ProcessChainContext ctx) {
		List<ACAutomatonResult> match = acAllWords.match(ctx.getDoc());
		List<String> wids = new LinkedList<String>();
		List<Long> w = new LinkedList<Long>();
		Long docId = ctx.getId();
		// 构建索引
		for (ACAutomatonResult result : match) {
			wids.add(result.getStr());
			w.add(result.getWordId());
			tempIndexMap.put(result.getWordId(), docId);
		}
		SelfLogger.log("调用链分析结果:did:"+docId+",wids:"+w+",url:"+ctx.getUrl()+"词:"+wids);
		return true;
	}

}
