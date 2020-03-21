package com.lahuan.common.env.query.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.env.query.QueryEnvManager;
import com.lahuan.query.entity.UrlInfo;

/**
 * 搜索页面处理
 * 
 * @author la-huan
 *
 */
public class ServerSearchHandler implements ServerHandler {
	/**
	 * 搜索页面URL
	 */
	private static final String DEFAULT_SEARCH_PREFIX = "/search/";
	/**
	 * 搜索页面URL的长度
	 */
	private static final int DEFAULT_SEARCH_PREFIX_LEN = DEFAULT_SEARCH_PREFIX.length();
	/**
	 * 默认响应头 200
	 */
	private static final String DEFAULT_HEADER = "HTTP/1.1 200 OK\n" + //
			"Server: Test\n" + //
			"Content-Type: text/html;charset=utf-8\n\n";

	public ServerSearchHandler(String prefixFilePath, String suffixFilePath) {
		try {
			prefix = new String(Files.readAllBytes(Paths.get(prefixFilePath)), GlobelConfig.DEFAULT_HTML_CHARSET);
			suffix = new String(Files.readAllBytes(Paths.get(suffixFilePath)), GlobelConfig.DEFAULT_HTML_CHARSET);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 前缀
	 */
	private String prefix;
	/**
	 * 后缀
	 */
	private String suffix;

	@Override
	public String handler(String req, String path, ServerContext ctx) {
		if (path.startsWith(DEFAULT_SEARCH_PREFIX) && path.length() > DEFAULT_SEARCH_PREFIX_LEN) {
			String words = path.substring(DEFAULT_SEARCH_PREFIX_LEN);
			try {
				words = URLDecoder.decode(words, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return buildHtml(words);
		}
		return null;
	}

	/**
	 * 构建返回信息
	 * 
	 * @param words
	 * @return
	 */
	public String buildHtml(String words) {
		StringBuilder sb = new StringBuilder();
		List<UrlInfo> result = null;
		try {
			result = QueryEnvManager.query(words);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 头
		sb.append(DEFAULT_HEADER);
		// 标题 等
		sb.append(prefix);
		sb.append("<h1>搜索内容：");
		sb.append(words);
		sb.append("</h1>");
		sb.append("<script type=\"text/javascript\">");
		sb.append("document.title = '搜索\"");
		sb.append(words);
		sb.append("\"的结果';</script>");
		// 搜索内容
		if (result == null) {
			sb.append("<h1>服务器未初始化或出现异常.</h1>");
		} else if (result.size() == 0) {
			sb.append("无匹配结果");
		} else {
			for (UrlInfo info : result) {
				sb.append(info.toHtmlTag());
				//break;
			}
		}

		sb.append(suffix);
		return sb.toString();
	}

	
}
