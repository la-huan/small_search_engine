package com.lahuan.common.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lahuan.analyse.tag.HtmlEmptyEnum;
import com.lahuan.analyse.tag.HtmlTagEndEnum;
import com.lahuan.analyse.tag.HtmlTagEnum;
import com.lahuan.analyse.tag.HtmlTagOneEnum;
import com.lahuan.analyse.tag.HtmlTagUselessEnum;
import com.lahuan.common.util.RequestUtil;
/**
 * HTML格式化工具类
 * @author la-huan
 *
 */
public class HTMLFormatUtil {

	public static void main(String[] args) throws Exception {
		byte[] rs = RequestUtil.sendGetForBytes("http://news.163.com/world");
		String str = new String(rs);
		String cs = HTMLFetchUtil.getHtmlCharset(str);
		System.out.println("cs:" + cs);
		String html = new String(rs, cs);
		System.out.println(html);
		List<String> urls = HTMLFetchUtil.findAllUrl(html);
		String title = HTMLFetchUtil.getTitle(html.toCharArray());
		String kw = HTMLFetchUtil.getKeywords(html.toCharArray());
		String desc = HTMLFetchUtil.getDescription(html.toCharArray());
		String doc = formatHtml(html);
		// doc = doc.replaceAll(" ", "");
		System.out.println("urls=======");
		urls.stream().forEach(System.out::println);
		System.out.println("result============");
		System.out.println(doc);
		System.out.println("title============");
		System.out.println(title);
		System.out.println("kw============");
		System.out.println(kw);
		System.out.println("desc============");
		System.out.println(desc);
	}

	// 标签模糊匹配
	private static ACAutomatonForTag tagMatch;
	// 要删除的标签对
	private static BMSearch[] removeStarts;
	private static BMSearch[] removeEnds;
	// body标签
	private static BMSearch bodyEnd = BMSearch.getInstance("</body>".toCharArray(), true);
	private static BMSearch bodyStart = BMSearch.getInstance("<body".toCharArray(), true);
	
	
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
		// System.out.println(tagWords);
		tagMatch = new ACAutomatonForTag(tagWords);
		// // 不需要的标签
		// char[] ifStart = "<!--[if ".toCharArray();
		// char[] ifEnd = "<![endif]-->".toCharArray();
		// char[] scriptStart = "<script".toCharArray();
		// char[] scriptEnd = "</script>".toCharArray();
		// char[] styleStart = "<style".toCharArray();
		// char[] styleEnd = "</style>".toCharArray();
		// char[] selectStart = "<select".toCharArray();
		// char[] selectEnd = "</select>".toCharArray();
		//
		// removeStarts = new BMSearch[] { BMSearch.getInstance(ifStart),
		// BMSearch.getInstance(scriptStart),
		// BMSearch.getInstance(styleStart), BMSearch.getInstance(selectStart)
		// };
		// removeEnds = new BMSearch[] { BMSearch.getInstance(ifEnd),
		// BMSearch.getInstance(scriptEnd),
		// BMSearch.getInstance(styleEnd), BMSearch.getInstance(selectEnd) };
		String[][] getUseleffTag = HtmlTagUselessEnum.GetUselessTag();
		removeStarts = new BMSearch[getUseleffTag.length];
		removeEnds = new BMSearch[getUseleffTag.length];
		for (int i = 0; i < getUseleffTag.length; i++) {
			removeStarts[i] = BMSearch.getInstance(getUseleffTag[i][0].toCharArray(), true);
			removeEnds[i] = BMSearch.getInstance(getUseleffTag[i][1].toCharArray(), true);
		}

	}

	/**
	 * 格式化HTML
	 * 
	 * @param doc
	 * @return
	 */
	public static String formatHtml(String doc) {
		// 只获取body里的东西
		String body = getBody(doc);
		// 去除js css if等
		String bodyClearUseLessTag = clearUselessTag(body);
		// 去除所有标签
		String res = clearAllTag(bodyClearUseLessTag);
		// 去除换行 多空格最多保留一个
		res = res.replaceAll("\n|\r", " ").replaceAll("\\s{2,}", " ");
		return res;
	}

	/**
	 * 获取body里的内容
	 * 
	 * @param doc
	 * @return
	 */
	private static String getBody(String doc) {
		char[] str = doc.toCharArray();
		int idx1 = bodyStart.searchText(str, 0);
		if (idx1 != -1) {
			for (int i = idx1 + 1; i < str.length; i++) {
				if (str[i] == '>') {
					idx1 = i + 1;
					break;
				}
			}
		}
		LinkedList<Integer> idx2 = bodyEnd.searchTextAll(str, 0);
		if (idx1 >= 0) {
			// 匹配到了
			if (idx2.size() > 0)
				return doc.substring(idx1, idx2.getLast());
			// 网络问题等非正常的情况 返回的是不完整的页面
			return doc.substring(idx1);
		}
		// 非规范的html标签 即不包含body对的 视为空的
		return "";
	}



	/**
	 * 去除不需要的标签
	 * 
	 * @param str
	 * @return
	 */
	private static String clearUselessTag(String str) {
		StringBuilder sb = new StringBuilder(str);
		char[] textCharArray = str.toCharArray();
		for (int i = 0; i < removeStarts.length; i++) {
			BMSearch matchStartCharrArray = removeStarts[i];
			BMSearch matchEndCharrArray = removeEnds[i];
			LinkedList<Integer> starts = matchStartCharrArray.searchTextAll(textCharArray, 0);
			// LinkedList<Integer> starts = BMSearch.bmAll(textCharArray,
			// matchStartCharrArray, 0);
			// 记录上一次
			// int prevEnd = Integer.MAX_VALUE;
			// 倒叙遍历
			Iterator<Integer> iterator = starts.descendingIterator();
			while (iterator.hasNext()) {
				Integer start = iterator.next();
				start += matchStartCharrArray.getMatchLength();
				int end = matchEndCharrArray.searchText(textCharArray, start);
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
	private static String clearAllTag(String doc) {
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

	


}
