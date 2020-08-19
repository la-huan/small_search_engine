package com.lahuan.query.urlearch;

import java.util.LinkedList;
import java.util.List;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.io.PersistenceIndex;
import com.lahuan.common.io.PersistenceOffset;
import com.lahuan.common.queue.ConcurrentFileQueue;
import com.lahuan.common.queue.SearchEngineFileQueue;
import com.lahuan.query.entity.ResultMapping;
import com.lahuan.query.entity.UrlInfo;

/**
 * 根据词ID获取URL 二分法
 * 
 * @author la-huan
 *
 */
public class URLBinarySearcher implements URLSearcher {
	// 持久化索引
	PersistenceIndex persistenceidx;
	// 持久化偏移量
	PersistenceOffset persistenceOffset;
	// url队列
	ConcurrentFileQueue queueUrlId;
	// 分析队列
	SearchEngineFileQueue queueAnalyse;
	// 分析队列
	ConcurrentFileQueue queueAnalyseReal;

	public URLBinarySearcher(PersistenceIndex persistenceidx, PersistenceOffset persistenceOffset,
			ConcurrentFileQueue queueUrlId, SearchEngineFileQueue queueAnalyse) {
		this.persistenceidx = persistenceidx;
		this.persistenceOffset = persistenceOffset;
		this.queueUrlId = queueUrlId;
		this.queueAnalyse = queueAnalyse;
		queueAnalyseReal = queueAnalyse.getQueue();
	}

	/**
	 * 根据词ID获取URL 的ID
	 */
	public List<ResultMapping> query(List<Long> ids) {
		List<ResultMapping> res = new LinkedList<ResultMapping>();
		for (Long id : ids) {
			query(id, res);

		}
		return res;
	}

	/**
	 * 根据词ID获取URL的ID
	 */
	public List<ResultMapping> query(Long[] ids) {
		List<ResultMapping> res = new LinkedList<ResultMapping>();
		for (Long id : ids) {
			query(id, res);

		}
		return res;

	}

	/**
	 * 根据词ID获取URL的ID
	 */
	private void query(Long id, List<ResultMapping> res) {
		long s = 0;// 索引下界
		long e = persistenceOffset.getMaxIdx();// 索引上界
		long m;// 中间索引
		ResultMapping mVal;// 中间索引的值
		while (s <= e) {
			m = s + (e - s) / 2;
			mVal = get(m);
			if (id > mVal.getWordId()) {
				s = m + 1;
			} else if (id < mVal.getWordId()) {
				e = m - 1;
			} else {
				// 匹配到了 结果全部放入
				res.add(mVal);
				return;
			}
		}
	}

	/**
	 * 根据索引获取值
	 * 
	 * @param i
	 * @return
	 */
	private ResultMapping get(long i) {
		long[] offset = persistenceOffset.readOffsetByIdx(i);
		byte[] read = persistenceidx.read(offset[0], offset[1]);
		return new ResultMapping(new String(read));
	}

	@Override
	public List<UrlInfo> query(ResultMapping m) {
		List<UrlInfo> res = new LinkedList<UrlInfo>();
		try {
			long[] htmls = m.getHtmls();
			for (long id : htmls) {

				byte[] bs = queueUrlId.randomGet(id);
				String url = new String(bs);
				String context = "";
				byte[] bs2 = queueAnalyseReal.randomGet(id);
				if (bs2 != null) {
					String[] doc = new String(bs2).split(GlobelConfig.ANALYSE_QUEUE_SEPARATE);
					if (doc.length > 2) {
						context = doc[2];
						// title + GlobelConfig.CONTENT_FORMAT_SEPARATE +
						// keywords + GlobelConfig.CONTENT_FORMAT_SEPARATE +
						// desc +
						// GlobelConfig.CONTENT_FORMAT_SEPARATE + formatHtml
						//
						// title keyword desc summary
						String[] result = context.split(GlobelConfig.CONTENT_FORMAT_SEPARATE);
						if(result.length==4){
							
							res.add(new UrlInfo(id, url, result[0], result[1], result[2], result[3]));
						}else{
							res.add(new UrlInfo(id, url, null, null,null, context));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

}
