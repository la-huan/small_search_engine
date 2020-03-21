package com.lahuan.query.wordfetch;

import java.util.List;

import com.lahuan.common.search.ACAutomatonResult;

/**
 * 根据文本获取分词 返回分词的ID
 * 
 * @author la-huan
 *
 */
public interface WordsSearcher {

	/**
	 * 根据文本获取分词 返回分词的ID
	 */
	public List<Long> queryId(String text);

	/**
	 * 根据文本获取分词 返回分词结果
	 */
	public List<ACAutomatonResult> query(String text);

	/**
	 * 判断是否存在词
	 */
	public boolean exist(String text);
}
