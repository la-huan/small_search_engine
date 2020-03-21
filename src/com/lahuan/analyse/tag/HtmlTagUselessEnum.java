package com.lahuan.analyse.tag;

import java.util.ArrayList;
import java.util.List;

/**
 * 不需要的标签集合
 * 
 * @author la-huan
 *
 */
public enum HtmlTagUselessEnum {
	/**
	 * if注释
	 */
	HTML_IF("<!--[if ", "<![endif]-->"),
	/**
	 * script代码
	 */
	HTML_SCRIPT("<script", "</script>"),
	/**
	 * css样式
	 */
	HTML_CSS("<style", "</style>"),
	/**
	 * select标签
	 */
	HTML_SELECT("<select", "</select>");

	/** 标签开始 */
	private String start;

	/** 标签结束形式1 */
	private String end;

	HtmlTagUselessEnum(String start, String end) {
		this.start = start;
		this.end = end;
	}

	public String getStart() {
		return start;
	}

	public String getEnd() {
		return end;
	}

	/**
	 * 获取开始标签集合
	 *
	 * @return 开始标签信合
	 */
	public static List<String> GetStartTagList() {

		List<String> getList = new ArrayList<>(values().length);

		for (HtmlTagUselessEnum tagSection : values()) {
			getList.add(tagSection.getStart());
		}

		return getList;
	}

	/**
	 * 获取结束标签集合
	 *
	 * @return 开始标签信合
	 */
	public static List<String> GetEndTagList() {

		List<String> getList = new ArrayList<>(values().length);

		for (HtmlTagUselessEnum tagSection : values()) {
			getList.add(tagSection.getEnd());
		}
		return getList;
	}

	/**
	 * 获取结束标签集合
	 *
	 * @return 开始标签信合
	 */
	public static String[][] GetUselessTag() {
		HtmlTagUselessEnum[] values = values();
		String[][] r = new String[values.length][];
		for (int i = 0; i < values.length; i++) {
			r[i] = new String[2];
			HtmlTagUselessEnum t = values[i];
			r[i][0] = t.getStart();
			r[i][1] = t.getEnd();
		}
		return r;
	}
	
}