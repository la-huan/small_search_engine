package com.lahuan.webcrawler.duplicationfilter;

/**
 * 调用java的hash
 * @author la-huan
 *
 */
public class JavaHash implements BloomHash{

	@Override
	public int hash(String url) {
		return url.hashCode() & (Integer.MAX_VALUE);
	}
	
	
}
