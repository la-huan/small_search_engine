package com.lahuan.common.search;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lahuan.analyse.tag.HtmlEmptyEnum;
import com.lahuan.analyse.tag.HtmlTagEndEnum;
import com.lahuan.analyse.tag.HtmlTagEnum;
import com.lahuan.analyse.tag.HtmlTagOneEnum;
import com.lahuan.common.util.RequestUtil;

// 暂存 无用
public class SearchUtil_temp {

	public static void main(String[] args) throws Exception {
		// StringBuilder sb =new StringBuilder("<div id=\"top\"><img ");
		// sb.delete(0, 14);
		// System.out.println(sb);
		testUrl3();
	}

	public static void testUrl3() throws Exception {
		byte[] rs = sendGet("https://www.qq.com/");
		String str = new String(rs);
		char[] charArray = str.toCharArray();
		String cs = getHtml4Charset(charArray);
		if(cs==null)
			cs = getHtml5Charset(charArray);
		System.out.println("cs:"+cs);
		String html = new String(rs,cs);
		System.out.println(html);
		String doc = formatHtml(html);
		doc = doc.replaceAll("\n|\r", " ");
		doc = doc.replaceAll(" {2,}", " ");
		//doc = doc.replaceAll("  ", "");
		System.out.println("============");
		System.out.println(doc);
	}
	public static void testUrl2() {
		String str = RequestUtil.sendGet("https://www.qq.com/", new HashMap<String, String>());
		System.out.println(str);
		String doc = formatHtml(str);
		// doc= doc.replace(" ", "");
		// doc= doc.replace(" ", "");
		// doc= doc.replace(" ", "");
		doc = doc.replaceAll("\\s*", "");
		System.out.println("===>");
		System.out.println(doc);
	}
	public static void testUrl() {
		String str = RequestUtil.sendGet("https://www.qq.com/", new HashMap<String, String>());
		List<String> res = SearchUtil_temp.getHtmlAllHref(str);
		System.out.println(res.size());
		res.stream().forEach(System.out::println);
	}
	
	public static String getHtmlCharset(String doc) {
		char[] html = doc.toCharArray();
		String cs = getHtml4Charset(html);
		if(cs==null)
			cs=getHtml5Charset(html);;
		if(cs==null)
			return "UTF-8";//无法识别的时候 当作utf8处理
		return cs;
	}
	private static String getHtml4Charset(char[] html) {
		//<meta  http-equiv="content-type" content="text/html;charset=UTF-8">
		char[] csStart = "http-equiv=\"content-type\"".toCharArray();
		char[] csStart2 = "content=\"".toCharArray();
		char[] csStart3 = "charset".toCharArray();
		char[] csEnd = "\"".toCharArray();
		int index1 = BMSearch.bm(html, csStart,0);
		if(index1 >=0) {
			int index2 = BMSearch.bm(html, csStart2,index1);
			if(index2>=0) {
				index2=index2+csStart2.length;
				int start = BMSearch.bm(html, csStart3,index2);
				if(start>=0) {	
					int end = BMSearch.bm(html, csEnd,index2);
					start+=(csStart3.length+1);
					end-=start;
					if(end>=0) {
						String res = new String(html,start,end);
						return res;
					}
					
				}
			}
			
		}
		return null;
		
	}
	private static String getHtml5Charset(char[] html) {
		// <meta charset="UTF-8">
		char[] csStart = "<meta".toCharArray();
		char[] csStart2 = "charset=\"".toCharArray();
		char[] csEnd = "\"".toCharArray();
		int index1 = BMSearch.bm(html, csStart,0);
		if(index1 >=0) {
			int start = BMSearch.bm(html, csStart2,index1);
			if(start >=0) {
				start+=(csStart2.length);
				int end = BMSearch.bm(html, csEnd,start);
				if(end>=0) {
					end-=start;
					String res = new String(html,start,end);
					return res;
				}
				
			}
		}
		return null;
	}
	public static String formatHtml(String doc) {
		// 只获取body里的东西
		String body = getBody(doc);
		//System.out.println(body);
		// 去除js css等
		String bodyClearUseLessTag = clearUselessTag(body);
		//System.out.println(bodyClearUseLessTag);
		// 去除所有标签
		return clearAllTag(bodyClearUseLessTag);
	}

	

	static ACAutomatonForTag tagMatch;
	static char[] bodyStart = "<body".toCharArray();
	static char[] bodyEnd = "</body>".toCharArray();
	static char[][] removeStarts;
	static char[][] removeEnds;
	
	static {
		LinkedList<String> tagWords = new LinkedList<String>();
		// 一对tag标签的
		List<String> tagStart = HtmlTagEnum.GetHtmlStartTagList();
		List<String> tagEnd = HtmlTagEnum.GetHtmlEndTagList();
		// 拼接带模糊匹配的后缀
		for (String word : tagStart) {
			tagWords.add(word + HtmlTagEndEnum.tagEndFuzzy.getEnd());
		}
		for (String word : tagEnd) {
			tagWords.add(word + HtmlTagEndEnum.tagEnd.getEnd());
		}
		// 单tag的
		List<String> oneTag = HtmlTagOneEnum.GetHtmlStartTagList();
		// 拼接带模糊匹配的后缀
		for (String word : oneTag) {
			tagWords.add(word + HtmlTagEndEnum.oneTagEndFuzzy.getEnd());
			tagWords.add(word + HtmlTagEndEnum.tagEndFuzzy.getEnd());
		}
		List<String> emptys = HtmlEmptyEnum.GetHtmlEmptyTagList();
		for (String word : emptys) {
			tagWords.add(word);
		}
		// 额外特殊考虑的 注释
		tagWords.add("<!--*-->");
		// 利用所有标签构造出AC自动机
		System.out.println(tagWords);
		tagMatch = new ACAutomatonForTag(tagWords);
		// 不需要的标签
		char[] scriptStart = "<script".toCharArray();
		char[] scriptEnd = "</script>".toCharArray();
		char[] styleStart = "<style".toCharArray();
		char[] styleEnd = "</style>".toCharArray();
		char[] selectStart = "<select".toCharArray();
		char[] selectEnd = "</select>".toCharArray();
		removeStarts = new char[][] { scriptStart, styleStart, selectStart };
		removeEnds = new char[][] { scriptEnd, styleEnd, selectEnd };
	}

	public static String getBody(String doc) {
		char[] str = doc.toCharArray();
		int idx1 = BMSearch.bm(str, bodyStart, 0);
		if (idx1 != -1) {
			for (int i = idx1 + 1; i < str.length; i++) {
				if (str[i] == '>') {
					idx1 = i + 1;
					break;
				}
			}
		}
		LinkedList<Integer> idx2 = BMSearch.bmAll(str, bodyEnd, 0);
		if (idx1 >= 0 && idx2.size() > 0)
			return doc.substring(idx1, idx2.getLast());
		// 非规范的html标签 即不包含body对的
		return doc;
	}

	public static String clearUselessTag(String str) {
		StringBuilder sb = new StringBuilder(str);
		char[] textCharArray = str.toCharArray();
		for (int i = 0; i < removeStarts.length; i++) {
			char[] matchStartCharrArray = removeStarts[i];
			char[] matchEndCharrArray = removeEnds[i];
			LinkedList<Integer> starts = BMSearch.bmAll(textCharArray, matchStartCharrArray, 0);
			// 记录上一次
			// int prevEnd = Integer.MAX_VALUE;
			// 倒叙遍历
			Iterator<Integer> iterator = starts.descendingIterator();
			while (iterator.hasNext()) {
				Integer start = iterator.next();
				start += matchStartCharrArray.length;
				int end = BMSearch.bm(textCharArray, matchEndCharrArray, start);
				if (end == -1) {// || end >= prevEnd
					continue;
				}
				sb.delete(start, end);
				// prevEnd = end;
			}
			// 每次都是替换后的串
			textCharArray = sb.toString().toCharArray();
		}
		return sb.toString();
	}

	/**
	 * 消除全部的tag
	 */
	public static String clearAllTag(String doc) {
		// System.out.println(str);
		char[] str = doc.toCharArray();
		LinkedList<ACAutomatonResult> match = tagMatch.match(str);
		StringBuilder sb = new StringBuilder();
		sb.append(str);
		Iterator<ACAutomatonResult> iterator = match.descendingIterator();
		while (iterator.hasNext()) {
			ACAutomatonResult res = iterator.next();
			// System.out.println(res);
			sb.delete(res.getPos(), res.getLen() + res.getPos());
		}

		return sb.toString();
	}

	/**
	 * 获取全部的URL 仅限href=后面的 TODO 可以考虑再增加更多精准的算法
	 * 
	 * @param text
	 * @return
	 */
	public static List<String> getHtmlAllHref(String str) {
		List<String> res = new ArrayList<String>();
		SearchUtil_temp.getHtmlBetween(str, "href=\"", "\"", res);
		SearchUtil_temp.getHtmlBetween(str, "href='", "'", res);
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
	public static byte[] sendGet(String full_url) {
		byte[] result = null;
		BufferedReader in = null;// 读取响应输入流
		try {
			
			//System.out.println(full_url);
			// 创建URL对象
			java.net.URL connURL = new java.net.URL(full_url);
			// 打开URL连接
			java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL.openConnection();
			// 设置通用属性
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Charsert", "utf-8");  
			//httpConn.setRequestProperty("Content-type", "text/html;charset=gbk");  
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");
			// 建立实际的连接
			httpConn.connect();
			// 响应头部获取
//			Map<String, List<String>> headers = httpConn.getHeaderFields();
//			// 遍历所有的响应头字段
//			for (String key : headers.keySet()) {
//				System.out.println(key + "\t：\t" + headers.get(key));
//			}
			// 定义BufferedReader输入流来读取URL的响应,并设置编码方式
			//in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
			InputStream is = httpConn.getInputStream();
			// 读取返回的内容
			result = new byte[is.available()];
			is.read(result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
