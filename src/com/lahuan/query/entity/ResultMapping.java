package com.lahuan.query.entity;

import java.util.Arrays;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.queue.ConcurrentFileQueue;
import com.lahuan.common.queue.SearchEngineFileQueue;
import com.lahuan.common.search.ACAutomaton;

/**
 * 结果的映射
 * 
 * @author la-huan
 *
 */
public class ResultMapping {

	long wordId;

	long[] htmls;

	public ResultMapping(String s) {
		// 1080[6, 65, 67, 84, 117, 127, 133]
		int idx = s.indexOf("[");
		String w = s.substring(0, idx);
		String h = s.substring(idx + 1, s.length() - 1);
		this.wordId = Long.valueOf(w);
		String[] split = h.split(",");
		this.htmls = new long[split.length];
		for (int i = 0; i < split.length; i++) {
			this.htmls[i] = Long.valueOf(split[i].trim());
		}
	}

	public long getWordId() {
		return wordId;
	}

	public long[] getHtmls() {
		return htmls;
	}
	//测试用 输出信息
	public String parse(ACAutomaton acAllWords, ConcurrentFileQueue queueUrlId, SearchEngineFileQueue queueAnalyse)
			throws Exception {
		StringBuilder res = new StringBuilder("word:" + acAllWords.getWordByid(wordId) + "\n");
		 ConcurrentFileQueue queue = queueAnalyse.getQueue();

		for (long l : htmls) {
			byte[] bs = queueUrlId.randomGet(l);
			String url = new String(bs);
			res.append(url);
			res.append("\n");
			byte[] bs2 = queue.randomGet(l);
			if (bs2 != null) {
				String[] doc = new String(bs2).split(GlobelConfig.ANALYSE_QUEUE_SEPARATE);
				if (doc.length > 2) {

					res.append(doc[2]);
					res.append("\n");
				}
			}
		}
		return res.toString();
	}

	@Override
	public String toString() {
		return "ResultMapping [wordId=" + wordId + ", htmls=" + Arrays.toString(htmls) + "]";
	}

}
