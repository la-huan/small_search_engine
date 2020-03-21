package com.lahuan.webcrawler.duplicationfilter;

import com.lahuan.common.util.MD5Util;

/**
 * md5实现hash
 * @author la-huan
 *
 */
public class Md5Hash implements BloomHash{
	private String salt="";
	public Md5Hash() {
	}
	public Md5Hash(String salt) {
		this.salt = salt;
	}
	
	@Override
	public int hash(String url) {
		// Integer.MAX_VALUE是掩码 屏蔽首位符号位 最终显示0或正数
		return MD5Util.string2MD5Salt(url,salt).hashCode() & Integer.MAX_VALUE;
	}

}
