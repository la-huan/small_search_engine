package com.lahuan.query.entity;

/**
 * url的信息
 * 
 * @author la-huan
 *
 */
public class UrlInfo {
	/**
	 * URL的ID
	 */
	long urlId;
	/**
	 * URL
	 */
	String url = "";
	/**
	 * 标题
	 */
	String title = "";
	/**
	 * 关键字
	 */
	String keyword = "";
	/**
	 * 描述
	 */
	String description = "";
	/**
	 * 文档内容
	 */
	String summary = "";

	public UrlInfo(long urlId, String url, String title, String keyword, String description, String summary) {
		super();
		this.urlId = urlId;
		this.url = url;
		this.title = title;
		this.keyword = keyword;
		this.description = description;
		this.summary = summary;
	}

	public long getUrlId() {
		return urlId;
	}

	public String getUrl() {
		return url;
	}

	public String getTitle() {
		if(title==null||title.equals(""))
			title="未知";
		return title;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getDescription() {
		return description;
	}

	public String getSummary() {
		return summary;
	}

	/**
	 * 拼接结果
	 * 
	 * @return
	 */
	public String toHtmlTag() {
		StringBuilder sb = new StringBuilder();
		sb.append("<div><br><p>标题:<a href=\"");
		sb.append(getUrl());
		sb.append("\" target=\"_blank\">");
		sb.append(getTitle());
		sb.append("</a></p>");
		sb.append("<p>URL_ID:");
		sb.append(getUrlId());
		sb.append("</p><p>URL:");
		sb.append(getUrl());
		sb.append("</p>");
		sb.append("<p>关键字:");
		sb.append(cutStrWithXMPTag(getKeyword()));
		sb.append("</p>");
		sb.append(" <p>描述:");
		sb.append(cutStrWithXMPTag(getDescription()));
		sb.append("</p>");
		sb.append("<p>内容摘要:");
		sb.append(cutStrWithXMPTag(getSummary()));
		sb.append("</p></div>");
		return sb.toString();
	}

	
	private static String cutStrWithXMPTag(String str) {
		return cutStrWithXMPTag(str, 120);
	}

	private static String cutStrWithXMPTag(String str, int length) {
		String finalStr = "";
		if (null == str || str.length() <= length) {
			finalStr = (str == null ? "" : str);
		} else {
			finalStr = str.substring(0, length) + "...";
		}
		return "<xmp>" + finalStr + "</xmp>";
	}
}
