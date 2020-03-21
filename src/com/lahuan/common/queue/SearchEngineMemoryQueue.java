package com.lahuan.common.queue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 将信息存在内存里的队列
 * 
 * @author la-huan
 *
 */
public class SearchEngineMemoryQueue implements SearchEngineQueue {

	protected LinkedBlockingQueue<String> queue;//访问类型写protected 可提供给子类修改
	protected int s =10;//单词最大拉取数量 访问类型写protected 可提供给子类修改
	
	public SearchEngineMemoryQueue(int size ){
		queue = new LinkedBlockingQueue<String>();// 初始化
		this.s=size;
	}
	public SearchEngineMemoryQueue() {
		queue = new LinkedBlockingQueue<String>();// 初始化
	}

	@Override
	public void send(String url) {
		queue.add(url);
	}

	public List<String> get() {
		List<String> res = new LinkedList<String>();
		String poll = queue.poll();
		//最大读取s个
		while (poll != null && res.size() < s) {
			res.add(poll);
			poll = queue.poll();
		}
		return res;
	}

	public void clear() {
		queue.clear();
	}

}
