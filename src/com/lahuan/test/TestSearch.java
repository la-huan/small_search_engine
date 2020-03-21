package com.lahuan.test;

import com.lahuan.common.env.query.QueryEnvManager;

public class TestSearch {
	public static void main(String[] args) throws Exception {
		// 初始化 使用文件夹test2
		QueryEnvManager.init("test2");
		// 查看全部
		// QueryEnv.checkAll(false);
		// 查找内容
		QueryEnvManager.queryAndPrint("开放平台服务");
	}
}
