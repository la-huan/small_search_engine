package com.lahuan.common.queue;

import java.util.List;

public interface SearchEngineQueue {
	/**
	 * 往消息队列存放 URL
	 * @param url
	 */
	public void send(String msg);
	/**
	 * 从消息队列获取
	 * @param s
	 * @return
	 */
	public List<String> get();
}
