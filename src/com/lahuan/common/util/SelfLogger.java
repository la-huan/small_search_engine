package com.lahuan.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自定义的LOG
 * 
 * @author la-huan
 *
 */
public class SelfLogger {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间
	private static FileWriteUtil log;// 日志
	private static ReentrantLock lock = new ReentrantLock();

	/**
	 * 初始化
	 * 
	 * @param path
	 */
	public static void init(String path) {
		log = new FileWriteUtil(path);// 日志
	}

	/**
	 * 写入日志
	 * 
	 * @param str
	 */
	public static void log(String str) {
		lock.lock();
		String msg = sdf.format(new Date()) + ":" + str + " \n";
		System.out.print(msg);
		log.write(msg.getBytes());
		lock.unlock();
	}

}
