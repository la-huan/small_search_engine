package com.lahuan.analyse.tag;

import java.util.ArrayList;
import java.util.List;
/**
 * 空元素
 * @author la-huan
 *
 */
public enum HtmlEmptyEnum {
	/**
	 * 不换行空格
	 */
	nbsp("&nbsp;"),
	/**
	 * 半角空格
	 */
	ensp("&ensp;"),
	/**
	 * 全角空格
	 */
	emsp("&emsp;"),
	/**
	 * 窄空格
	 */
	thinsp("&thinsp;"),
	/**
	 * 零宽不连字
	 */
	zwnj("&zwnj;"),
	/**
	 * 零宽连字
	 */
	zwj("&zwj;");
	private String value;

	HtmlEmptyEnum(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	/**
	 * 获取网页结束标签集合
	 *
	 * @return 开始标签信合
	 */
	public static List<String> GetHtmlEmptyTagList() {

		List<String> getList = new ArrayList<>(values().length);

		for (HtmlEmptyEnum tagSection : values()) {
			getList.add(tagSection.getValue());
		}

		return getList;
	}
}
