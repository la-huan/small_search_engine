package com.lahuan.common.util;

import java.security.MessageDigest;

/**
 * 采用MD5加密解密
 */
public class MD5Util {
	public static final String salt = "salt";// 盐
	/***
	 * MD5加码 生成32位md5码 简易加盐
	 */
	public static String string2MD5Salt(CharSequence rawPassword){
		return string2MD5Salt(rawPassword, salt);
	}

	/***
	 * MD5加码 生成32位md5码 简易加盐
	 */
	public static String string2MD5Salt(CharSequence rawPassword, String salt) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		// 简单加盐
		char[] charArray = (rawPassword.toString() + salt).toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();

	}

	/***
	 * MD5加码 生成32位md5码
	 */
	public static String string2MD5(CharSequence rawPassword) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
			return "";
		}
		char[] charArray = rawPassword.toString().toCharArray();
		byte[] byteArray = new byte[charArray.length];

		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	/**
	 * 根据自己的规则进行MD5加密 例如，现在需求是有字符串传入zhang，xy 其中zhang是传入的字符
	 * 然后需要将zhang的字符进行拆分z，和hang， 最后需要加密的字段为zxyhang
	 */
	public static String MD5Test(String inStr) {
		String xy = "xy";
		String finalStr = "";
		if (inStr != null) {
			String fStr = inStr.substring(0, 1);
			String lStr = inStr.substring(1, inStr.length());
			finalStr = string2MD5(fStr + xy + lStr);

		} else {
			finalStr = string2MD5(xy);
		}
		return finalStr;
	}

	
}