package com.lahuan.webcrawler.duplicationfilter;

/**
 * 简易的哈希算法
 *
 */
public class SimpleHash implements BloomHash {
	private int seed;

	public SimpleHash(int seed) {
		// this.cap = cap;
		this.seed = seed;
	}

	public int hash(String url) {
		int res = 0;
		for (int i = 0; i <  url.length(); i++) {
			res = seed * res + url.charAt(i);
		}
		// Integer.MAX_VALUE是掩码 屏蔽首位符号位 最终显示0或正数
		return Integer.MAX_VALUE & res;
	}
}