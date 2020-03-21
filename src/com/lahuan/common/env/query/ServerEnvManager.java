package com.lahuan.common.env.query;

import java.util.LinkedList;
import java.util.List;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.env.query.server.Server;
import com.lahuan.common.env.query.server.ServerDefaultHandler;
import com.lahuan.common.env.query.server.ServerHandler;
import com.lahuan.common.env.query.server.ServerIndexHandler;
import com.lahuan.common.env.query.server.ServerSearchHandler;
import com.lahuan.common.env.query.server.SmallWebServer;

public class ServerEnvManager {
	/**
	 * 初始化
	 */
	public static void init(String str, int port) {
		QueryEnvManager.init(str);
		initial(port);
	}

	/**
	 * 初始化
	 */
	public static void init(int port) {
		QueryEnvManager.init();
		initial(port);
	}
	/**
	 * 启动服务器
	 */
	public static void start(){
		serverObject.start();
	}
	// 服务器处理对象集合
	private static List<ServerHandler> serverHandlerList;
	// 服务器默认处理对象
	private static ServerHandler serverDefaultHandler;
	// 服务器对象
	private static Server serverObject;

	/**
	 * 初始化
	 */
	private static void initial(int port) {
		// 服务器处理对象集合
		serverHandlerList = getServerHandlerList();
		QueryGlobelContext.put(GlobelConfig.SERVER_HANDLERS_LIST, serverHandlerList);
		// 服务器默认处理对象
		serverDefaultHandler = new ServerDefaultHandler();
		QueryGlobelContext.put(GlobelConfig.SERVER_DEFAULT_HANDLER, serverDefaultHandler);
		// 服务器对象
		serverObject = new SmallWebServer(port, serverDefaultHandler, serverHandlerList, 1024);
		QueryGlobelContext.put(GlobelConfig.SERVER_OBJECT, serverObject);
	}
	
	/**
	 * 获取服务器处理对象集合
	 * 
	 * @return
	 */
	private static List<ServerHandler> getServerHandlerList() {
		List<ServerHandler> serverHandlerList = new LinkedList<ServerHandler>();
		serverHandlerList.add(new ServerIndexHandler(GlobelConfig.SERVER_FILE_INDEX));
		serverHandlerList.add(new ServerSearchHandler(GlobelConfig.SERVER_FILE_RESULT_PREFIX,
				GlobelConfig.SERVER_FILE_RESULT_SUFFIX));
		return serverHandlerList;
	}
}
