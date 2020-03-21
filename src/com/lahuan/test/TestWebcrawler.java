package com.lahuan.test;

import com.lahuan.common.env.webcrawler.WebcrawlerEnvManager;

public class TestWebcrawler {

	public static void main(String[] args) {
		// 初始化  使用文件夹test2
		WebcrawlerEnvManager.init("test2");
		// 开始查找
		WebcrawlerEnvManager.start();
		//爬取阈值在com.lahuan.common.config.GlobelConfig.MONITOR_THREAD_THRESHOLD
				//当分析队列达到阈值后，就会停止（定时检测）。
	}
}
