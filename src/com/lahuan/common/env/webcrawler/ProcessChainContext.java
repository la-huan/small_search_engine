package com.lahuan.common.env.webcrawler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 调用链上下文
 * 
 * @author la-huan
 *
 */
public class ProcessChainContext {
	
	
	public ProcessChainContext(String url, String doc,Long id ) {
		this.url = url;
		this.doc = doc;
		this.id=id;
	}
	public ProcessChainContext(String url, String doc ) {
		this.url = url;
		this.doc = doc;
	}
	/**
	 * url
	 */
	private String url;
	/**
	 * 文档内容
	 */
	private String doc;// 文档内容
	/**
	 * id
	 */
	private Long id;
	/**
	 * 上下文参数
	 */
	private ConcurrentHashMap<String, Object> context = new ConcurrentHashMap<String, Object>();// 上下文参数

	/**
	 * URL
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public Long getId() {
		return id;
	}

	/**
	 * 文档
	 * 
	 * @return
	 */
	public String getDoc() {
		return doc;
	}
	

	/**
	 * 获取上下文参数
	 * 
	 * @param key
	 * @return
	 */
	public Object get(String key) {
		return context.get(key);
	}

	/**
	 * 设置上下文参数
	 * 
	 * @param key
	 * @param val
	 */
	public void set(String key, Object val) {
		context.put(key, val);
	}
	
	

	@Override
	public String toString() {
		return "ProcessChainContext [url=" + url + ", doc=" + doc + ", context=" + context + "]";
	}
	
	
}
