package com.lahuan.common.env.query.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.env.query.QueryEnvManager;
import com.lahuan.common.util.SelfLogger;

public class SmallWebServer implements Server {
	public static void main(String[] args) {
		List<ServerHandler> hs = new LinkedList<ServerHandler>();
		hs.add(new ServerIndexHandler(GlobelConfig.SERVER_FILE_INDEX));
		hs.add(new ServerSearchHandler(GlobelConfig.SERVER_FILE_RESULT_PREFIX, GlobelConfig.SERVER_FILE_RESULT_SUFFIX));
		QueryEnvManager.init("test2");
		SmallWebServer wsd = new SmallWebServer(12555, new ServerDefaultHandler(), hs, 1024);
		wsd.start();
	}

	/**
	 * 默认处理
	 */
	public ServerHandler defaultHandler;
	/**
	 * 操作
	 */
	public List<ServerHandler> handlers;
	/**
	 * 默认响应头 200
	 */
	protected static final String DEFAULT_HEADER = "HTTP/1.1 200 OK\n" + //
			"Server: Test\n" + //
			"Content-Type: text/html;charset=utf-8\n\n";

	/**
	 * 请求报文最大读取长度 单位byte
	 */
	private int requestMaxSize = 1024;
	/**
	 * 端口
	 */
	private int port;

	public SmallWebServer(int port, ServerHandler defaultHandler, List<ServerHandler> handlers, int requestMaxSize) {
		this.port = port;
		this.defaultHandler = defaultHandler;
		this.handlers = handlers;
		this.requestMaxSize = requestMaxSize;
	}

	public SmallWebServer(int port, ServerHandler defaultHandler, List<ServerHandler> handlers) {
		this.port = port;
		this.defaultHandler = defaultHandler;
		this.handlers = handlers;
	}

	public SmallWebServer(int port, ServerHandler defaultHandler, int requestMaxSize) {
		this.port = port;
		this.defaultHandler = defaultHandler;
		this.requestMaxSize = requestMaxSize;
		handlers = new LinkedList<ServerHandler>();
	}

	public SmallWebServer(int port, ServerHandler defaultHandler) {
		this.port = port;
		this.defaultHandler = defaultHandler;
		handlers = new LinkedList<ServerHandler>();
	}

	/**
	 * 默认处理类
	 * 
	 * @param defaultHandler
	 */
	public void setDefaultHandler(ServerHandler defaultHandler) {
		this.defaultHandler = defaultHandler;
	}

	/**
	 * 会按照插入顺序 调用处理类
	 * 
	 * @param e
	 */
	public void addHandler(ServerHandler e) {
		this.handlers.add(e);
	}

	public void start() {
		try {
			ServerSocketChannel ssc = ServerSocketChannel.open();
			ssc.socket().bind(new InetSocketAddress(port));
			ssc.configureBlocking(false);
			Selector selector = Selector.open();
			// 注册 channel，并且指定感兴趣的事件是 Accept
			ssc.register(selector, SelectionKey.OP_ACCEPT);
			SelfLogger.log("server start,port:" + port);
			while (true) {
				selector.select();
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						// 创建新的连接，并且把连接注册到selector上，而且，
						// 声明这个channel只对读操作感兴趣。
						SocketChannel socketChannel = ssc.accept();
						socketChannel.configureBlocking(false);
						socketChannel.register(selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						SocketChannel socketChannel = null;
						try {
							socketChannel = (SocketChannel) key.channel();
							ByteBuffer readBuff = ByteBuffer.allocate(requestMaxSize);
							readBuff.clear();
							socketChannel.read(readBuff);
							readBuff.flip();
							String req = new String(readBuff.array());
							// System.out.println("响应报文=====");
							// System.out.println(req);
							// System.out.println("=====");
							// 获取URL
							int idx = req.indexOf("\n");
							if (idx == -1) {
								// 非法情况
								socketChannel.close();
							} else {
								String line1 = req.substring(0, idx);
								String[] split = line1.split(" ");
								if (split.length == 3) {
									// 提取其中的url 然后交给写事件
									String path = split[1];
									ServerContext ctx = new ServerContext(req, path);
									key.attach(ctx);
									key.interestOps(SelectionKey.OP_WRITE);
								} else {
									// 非法情况
									socketChannel.close();
								}

							}
						} catch (Exception e) {
							SelfLogger.log("SmallWebServer read error:" + Arrays.toString(e.getSuppressed()) + ",msg:"
									+ e.getMessage());
							e.printStackTrace();
							if (socketChannel != null)
								socketChannel.close();
						}
					} else if (key.isWritable()) {
						SocketChannel socketChannel = null;
						try {
							socketChannel = (SocketChannel) key.channel();

							ServerContext ctx = (ServerContext) key.attachment();
							String res = null;
							// 处理
							for (ServerHandler handle : handlers) {
								try {
									res = handle.handler(ctx.getRequest(), ctx.getPath(), ctx);
									if (res != null) {
										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							// 默认处理
							if (res == null) {
								try {
									res = defaultHandler.handler(ctx.getRequest(), ctx.getPath(), ctx);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							// 未返回出任何数据
							if (res == null) {
								res = DEFAULT_HEADER + "none";
							}
							byte[] bytes = res.getBytes();
							ByteBuffer writeBuff = ByteBuffer.allocate(bytes.length);
							// 返回响应报文
							writeBuff.put(bytes);
							writeBuff.flip();
							writeBuff.rewind();
							socketChannel.write(writeBuff);
							socketChannel.close();
							// key.interestOps(SelectionKey.OP_READ);
						} catch (Exception e) {
							SelfLogger.log("SmallWebServer write error:" + Arrays.toString(e.getSuppressed()) + ",msg:"
									+ e.getMessage());
							e.printStackTrace();
							if (socketChannel != null)
								socketChannel.close();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			SelfLogger
					.log("SmallWebServer start error:" + Arrays.toString(e.getSuppressed()) + ",msg:" + e.getMessage());

		}
	}

}