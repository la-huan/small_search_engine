package com.lahuan.common.util;

/**
 * 常用类型的工具类
 * 
 * @author la-huan
 *
 */
public class BaseTypeUtil {

	public static String cutStr(String str, int length) {
		String finalStr = "";
		if (null == str || str.length() <= length) {
			finalStr = (str == null ? "" : str);
		} else {
			finalStr = str.substring(0, length) + "...";
		}
		return finalStr;
	}
	
}
