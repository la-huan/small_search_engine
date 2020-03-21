package com.lahuan.common.util;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegularUtil {

	/**
	 * 获取HTTP的title
	 * 
	 * @param text
	 * @return
	 */
	public static String getHttpTitle(String text) {
		StringBuilder res = new StringBuilder();
		Pattern pattern = Pattern.compile("(?is)(?<=<(title|TITLE|Title)>).*(?=</(title|TITLE|Title)>)");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res.append(matcher.group());
		}

		return res.toString();
	}

	/**
	 * 获取HTTP的BODY
	 * 
	 * @param text
	 * @return
	 */
	public static String getHttpBody(String text) {
		StringBuilder res = new StringBuilder();

		Pattern pattern = Pattern.compile("(?is)(?<=<(body|BODY|Body)>).*(?=</(body|BODY|Body)>)");

		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res.append(matcher.group());
		}
		return res.toString();
	}

	/**
	 * 清除全部的标签
	 * 
	 * @param text
	 * @return
	 */
	public static String clearHttpTag(String text) {
		String res = "";
		String regex = "/<\\/?.+?\\/?>/g";
		res = text.replaceAll(regex, "");
		return res;
	}

	/**
	 * 获取全部的URL 仅限HTTP和HTTPS的URL
	 * 
	 * @param text
	 * @return
	 */
	public static LinkedList<String> findAllUrl(String text) {
		LinkedList<String> res = new LinkedList<String>();
		Pattern pattern = Pattern.compile("(http|https)://[\\w\\.]+[:\\d]?[/\\w]+\\??[\\w=&?]+");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res.add(matcher.group());
		}
		return res;
	}
}
