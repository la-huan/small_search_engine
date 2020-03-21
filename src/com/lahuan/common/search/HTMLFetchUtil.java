package com.lahuan.common.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lahuan.common.config.GlobelConfig;

/**
 * HTML信息获取的工具类
 * 
 * @author la-huan
 *
 */
public class HTMLFetchUtil {

	// HTML4的匹配
	// <meta http-equiv="content-type" content="text/html;charset=UTF-8">
	private static BMSearch cs4Start = BMSearch.getInstance("http-equiv=\"content-type\"".toCharArray(), true);
	private static BMSearch cs4Start2 = BMSearch.getInstance("content=\"".toCharArray(), true);
	private static BMSearch cs4Start3 = BMSearch.getInstance("charset=".toCharArray(), true);
	private static BMSearch cs4End = BMSearch.getInstance("\"".toCharArray(), true);

	// HTML5的匹配
	//// <meta charset="UTF-8">
	private static BMSearch cs5Start = BMSearch.getInstance("<meta".toCharArray(), true);
	private static BMSearch cs5Start2 = BMSearch.getInstance("charset=\"".toCharArray(), true);
	private static BMSearch cs5End = BMSearch.getInstance("\"".toCharArray(), true);

	// 获取keywords用的
	private static BMSearch kwStart = BMSearch.getInstance("<meta".toCharArray(), true);
	private static BMSearch kwStart2 = BMSearch.getInstance("name=\"keywords\"".toCharArray(), true);
	private static BMSearch kwStart3 = BMSearch.getInstance("content=\"".toCharArray(), true);
	private static BMSearch kwEnd = BMSearch.getInstance("\"".toCharArray(), true);

	// 获取description用的
	private static BMSearch descStart = BMSearch.getInstance("<meta".toCharArray(), true);
	private static BMSearch descStart2 = BMSearch.getInstance("name=\"description\"".toCharArray(), true);
	private static BMSearch descStart3 = BMSearch.getInstance("content=\"".toCharArray(), true);
	private static BMSearch descEnd = BMSearch.getInstance("\"".toCharArray(), true);
	// title标签
	private static BMSearch titleEnd = BMSearch.getInstance("</title>".toCharArray(), true);
	private static BMSearch titleStart = BMSearch.getInstance("<title".toCharArray(), true);

	/**
	 * 获取HTML编码
	 * 
	 * @param doc
	 * @return
	 */
	public static String getHtmlCharset(String doc) {
		char[] html = doc.toCharArray();
		String cs = getHtml4Charset(html);
		if (cs == null || cs.length() == 0)
			cs = getHtml5Charset(html);
		if (cs == null || cs.length() == 0)
			return GlobelConfig.DEFAULT_HTML_CHARSET;// 无法识别的时候 当作默认编码处理 utf-8
		return cs;
	}

	/**
	 * 获取HTML4编码
	 * 
	 * @param doc
	 * @return
	 */
	private static String getHtml4Charset(char[] html) {
		// <meta http-equiv="content-type" content="text/html;charset=UTF-8">
		// <meta http-equiv="Content-Type" content="text/html; charset=gbk">
		int index1 = cs4Start.searchText(html, 0);
		if (index1 >= 0) {
			int index2 = cs4Start2.searchText(html, index1);
			if (index2 >= 0) {
				index2 = index2 + cs4Start2.getMatchLength();
				int start = cs4Start3.searchText(html, index2);
				if (start >= 0) {
					int end = cs4End.searchText(html, index2);
					start += (cs4Start3.getMatchLength());
					end -= start;
					if (end >= 0) {
						String res = new String(html, start, end);
						return res;
					}

				}
			}

		}
		return null;
	}

	/**
	 * 获取HTML5编码
	 * 
	 * @param doc
	 * @return
	 */
	private static String getHtml5Charset(char[] html) {
		// <meta charset="UTF-8">
		int index1 = cs5Start.searchText(html, 0);
		if (index1 >= 0) {
			int start = cs5Start2.searchText(html, index1);
			if (start >= 0) {
				start += (cs5Start2.getMatchLength());
				int end = cs5End.searchText(html, start);
				if (end >= 0) {
					end -= start;
					String res = new String(html, start, end);
					return res;
				}
			}
		}
		return null;
	}

	/**
	 * 获取keywords
	 */
	public static String getKeywords(char[] html) {
		// <meta name="keywords" content="天气,新闻" />
		int index1 = kwStart.searchText(html, 0);
		if (index1 >= 0) {
			int index2 = kwStart2.searchText(html, index1);
			if (index2 >= 0) {
				index2 = index2 + kwStart2.getMatchLength();
				int start = kwStart3.searchText(html, index2);
				if (start >= 0) {
					int end = kwEnd.searchText(html, start + kwStart3.getMatchLength());
					start += (kwStart3.getMatchLength());
					end -= start;
					if (end >= 0) {
						String res = new String(html, start, end);
						return res;
					}

				}
			}

		}
		return "";
	}

	/**
	 * 获取description
	 */
	public static String getDescription(char[] html) {
		// <meta name="description" content="天气新闻" />
		// <meta name="keywords" content="天气,新闻" />
		int index1 = descStart.searchText(html, 0);
		if (index1 >= 0) {
			int index2 = descStart2.searchText(html, index1);
			if (index2 >= 0) {
				index2 = index2 + descStart2.getMatchLength();
				int start = descStart3.searchText(html, index2);
				if (start >= 0) {
					int end = descEnd.searchText(html, start + descStart3.getMatchLength());
					start += (descStart3.getMatchLength());
					end -= start;
					if (end >= 0) {
						String res = new String(html, start, end);
						return res;
					}

				}
			}

		}
		return "";
	}

	/**
	 * 获取全部的URL 仅限href=后面的 TODO 可以考虑再增加更多精准的算法
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> getHtmlAllHref(String str) {
		List<String> res = new ArrayList<String>();
		getHtmlBetween(str, "href=\"", "\"", res);
		getHtmlBetween(str, "href='", "'", res);
		return res;
	}

	/**
	 * 获取在两个字符串之间的字符
	 * 
	 * @param str
	 * @param s
	 * @param e
	 * @param res
	 */
	private static void getHtmlBetween(String str, String s, String e, List<String> res) {
		List<Integer> starts = BMSearch.bmAll(str, s);
		for (int i = 0; i < starts.size(); i++) {
			Integer start = starts.get(i);
			start += s.length();
			int end = BMSearch.bm(str, e, start);
			// 合理情况的判断
			if (end != -1 && (i == starts.size() - 1 || starts.get(i + 1) > end)) {
				String r = str.substring(start, end);
				if (r.startsWith("http") || r.startsWith("https"))
					res.add(r);
			}
		}
	}

	/**
	 * 正则获取全部的URL 仅限HTTP和HTTPS的URL
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> findAllUrl(String text) {
		ArrayList<String> res = new ArrayList<String>();
		Pattern pattern = Pattern.compile("(http|https)://[\\w\\.]+[:\\d]?[/\\w]+\\??[\\w=&?]+");
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res.add(matcher.group());
		}
		return res;
	}

	/**
	 * 获取body里的内容
	 * 
	 * @param doc
	 * @return
	 */
	public static String getTitle(char[] str) {
		// = doc.toCharArray();
		int idx1 = titleStart.searchText(str, 0);
		if (idx1 != -1) {
			for (int i = idx1 + 1; i < str.length; i++) {
				if (str[i] == '>') {
					idx1 = i + 1;
					break;
				}
			}
		}
		int idx2 = titleEnd.searchText(str, 0);
		if (idx1 >= 0 && idx2 > 0 && idx2 > idx1) {
			// 匹配到了
			return new String(str, idx1, idx2 - idx1);
			// 网络问题等非正常的情况 返回的是不完整的页面
		}
		// 非规范的html标签 即不包含body对的 视为空的
		return "";
	}
}
