package com.lahuan.common.env.query.server;
/**
 * 服务器处理方法上下文
 * @author la-huan
 *
 */
public class ServerContext {
	/**
	 * 请求报文
	 */
	String request;
	/**
	 * path
	 */
	String path;
	/**
	 * 附加信息
	 */
	Object attachment;

	public ServerContext(String request, String path) {
		this.request = request;
		this.path = path;
	}

	public ServerContext(String request, String path, Object attachment) {
		this.request = request;
		this.path = path;
		this.attachment = attachment;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}

}
