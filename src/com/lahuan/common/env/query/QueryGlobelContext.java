package com.lahuan.common.env.query;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 查找环境全局上下文
 * @author la-huan
 *
 */
public class QueryGlobelContext {

	// map
	private static ConcurrentHashMap<String, Object> ctx = new ConcurrentHashMap<String, Object>();

	public static void put(String k, Object v) {
		ctx.put(k, v);
	}

	public static Object get(String k) {
		return ctx.get(k);
	}

}
