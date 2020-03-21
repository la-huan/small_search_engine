package com.lahuan.test;

import com.lahuan.common.env.query.ServerEnvManager;

public class TestServer {
	public static void main(String[] args) throws Exception {
		//使用文件夹test2,服务器端口12555
		ServerEnvManager.init("test2", 12555);
		ServerEnvManager.start();
	}
}
