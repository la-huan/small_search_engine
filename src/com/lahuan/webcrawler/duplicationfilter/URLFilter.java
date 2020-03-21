package com.lahuan.webcrawler.duplicationfilter;

public interface URLFilter {
	
	/**
	 * 返回过滤后的结果
	 * @param url
	 * @return true 代表可以继续 false就结束
	 */
	public boolean filter(String url);
}
