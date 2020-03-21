package com.lahuan.common.env.query.server;

/**
 * 默认页面处理
 * 
 * @author la-huan
 *
 */
public class ServerDefaultHandler implements ServerHandler {
	// 重定向报文
	private static final String RESULT = "HTTP/1.1 302 Moved Temporarily\n" + //
			"Location: /index";

	@Override
	public String handler(String req,String path, ServerContext ctx) {
		return RESULT;
	}

}
