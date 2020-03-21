package com.lahuan.webcrawler.duplicationfilter;

/**
 * 给布隆过滤器用的 hash
 * @author la-huan
 *
 */
public interface BloomHash {
	/**
	 * 给布隆过滤器用的 hash
	 * 要求返回值在在0x00000000~0x7FFFFFFF之间(正数以及0)
	 * @param url
	 * @return
	 */
	int hash(String url);
	
}
