package com.lahuan.analyse.tempindex;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

public class TempIndexMemoryHashMap<k, v> implements TempIndexMap<k, v> {

	ConcurrentHashMap<k, Collection<v>> map = new ConcurrentHashMap<k, Collection<v>>();//map
	AtomicInteger keySize = new AtomicInteger(0);//key的大小
	AtomicInteger valueSize = new AtomicInteger(0);//value的大小
	@Override
	public void put(k key, v value) {
		//使用线程安全的set 同时还能帮助去重
		Collection<v> list = map.get(key);
		if (list == null) {
			synchronized (map) {
				if (map.get(key) == null) {
					list = new CopyOnWriteArraySet<v>();//线程安全的set
					map.put(key, list);
					//数量增加
					keySize.getAndIncrement();
				}
			}
		}
		//总数量增加
		valueSize.incrementAndGet();
		list.add(value);
	}

	@Override
	public void put(k key, v[] values) {
		//循环掉用
		for (v value : values) {
			put(key, value);
		}
	}

	@Override
	public void put(k key, Collection<v> values) {
		//循环掉用
		for (v value : values) {
			put(key, value);
		}
	}

	@Override
	public Map<k, Collection<v>> getAll() {
		return map;
	}

	@Override
	public int getKeySize() {
		return keySize.get();
	}

	@Override
	public int getValueSize() {
		return valueSize.get();
	}

}
