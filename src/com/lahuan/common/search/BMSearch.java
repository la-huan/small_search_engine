package com.lahuan.common.search;

import java.util.HashMap;
import java.util.LinkedList;
/**
 * BM匹配算法
 * @author la-huan
 *
 */
public class BMSearch {

	public static void main(String[] args) {
		// BMSearch k = new BMSearch();
		Object r = BMSearch.bmAll("ababa", "aba", 0);
		System.out.println(r);

	}

	HashMap<Character, Integer> bc;// 坏字符字典
	boolean[] prefix;// 好前缀字典
	int[] suffix;// 好字符字典
	char[] match;// 模式串
	private boolean ignoreCase = false;// 是否忽视大小写 开启后全部转为大写进行匹配

	private BMSearch(char[] b, boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
		match = new char[b.length];
		if (ignoreCase) {
			for (int i = 0; i < b.length; i++) {
				match[i] = Character.toUpperCase(b[i]);
			}
		} else {
			for (int i = 0; i < b.length; i++) {
				match[i] = b[i];
			}
		}
		int m = b.length;
		bc = buildBc(match, m);// 坏字符字典
		prefix = new boolean[b.length];// 好前缀字典
		suffix = new int[b.length];// 好字符字典
		buildGc(b, m, suffix, prefix);// 好支付构建
	}

	/**
	 * 获取模式串大小
	 * 
	 * @return
	 */
	public int getMatchLength() {
		return match.length;
	}

	/**
	 * 获取实例对象
	 * 
	 * @param match
	 * @return
	 */
	public static BMSearch getInstance(char[] match, boolean ignoreCase) {
		if (ignoreCase) {
			return new BMSearch(match, true);
		}
		return new BMSearch(match, false);
	}

	/**
	 * 获取实例对象
	 * 
	 * @param match
	 * @return
	 */
	public static BMSearch getInstance(char[] match) {
		return new BMSearch(match, false);
	}

	/**
	 * bm算法 匹配全部
	 * 
	 * @author lfy
	 * @return
	 */
	public LinkedList<Integer> searchTextAll(String text) {
		return searchTextAll(text.toCharArray(), 0);
	}

	/**
	 * bm算法
	 * 
	 * @author lfy
	 * @return
	 */
	public int searchText(String text) {
		return searchText(text.toCharArray(), 0);
	}

	/**
	 * bm算法 匹配全部
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public LinkedList<Integer> searchTextAll(char[] text, int pos) {
		// int pos = 0;// 主串位置偏移量
		LinkedList<Integer> res = new LinkedList<Integer>();
		// int pos = 0;// 偏移量
		int n = text.length;
		int m = match.length;
		char[] a = new char[n];
		//是否忽视大小写
		if (ignoreCase) {
			for (int i = 0; i < a.length; i++) {
				a[i] = Character.toUpperCase(text[i]);
			}
		} else {
			for (int i = 0; i < a.length; i++) {
				a[i] = text[i];
			}
		}
		while (pos <= n - m) {// 偏移量未到底的时候
			int idx = m - 1;// 当前模式串的最后坐标
			for (; idx >= 0; idx--) {
				// 模式串和主串的末尾部分相不匹配的时候
				if (match[idx] != a[idx + pos]) {
					break;
				}
			}
			if (idx < 0) {// 匹配
				res.add(pos);
				idx++;// 这里当成可匹配字符减1 即从下一个字符开始匹配
				// return pos;
			}

			char bad = a[pos + idx];// 坏字符
			// int badIndex = bad;// 坏字符字典索引
			// 查找坏字符所在的最后一个位置
			Integer bcid = bc.get(bad);
			int badIdx = bcid != null ? bcid : -1;
			// 坏字符偏移量
			int badPos = idx - badIdx;
			int goodPos = 0;
			if (idx < m - 1) {// 有好字符
				goodPos = getGoodPos(idx, m, suffix, prefix);
			}
			// 得出更大的偏移量进行偏移
			int max = Math.max(badPos, goodPos);
			pos = pos + max;
		}

		return res;
	}

	/**
	 * bm算法
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public int searchText(char[] text, int pos) {
		// int pos = 0;// 偏移量
		int n = text.length;
		int m = match.length;
		char[] a = new char[n];
		//是否忽视大小写
		if (ignoreCase) {
			for (int i = 0; i < a.length; i++) {
				a[i] = Character.toUpperCase(text[i]);
			}
		} else {
			for (int i = 0; i < a.length; i++) {
				a[i] = text[i];
			}
		}

		while (pos <= n - m) {// 偏移量未到底的时候
			int idx = m - 1;// 当前模式串的最后坐标
			for (; idx >= 0; idx--) {
				// 模式串和主串的末尾部分相不匹配的时候
				if (match[idx] != a[idx + pos]) {
					break;
				}
			}
			if (idx < 0) {// 匹配
				return pos;
			}

			char bad = a[pos + idx];// 坏字符
			// int badIndex = bad;// 坏字符字典索引
			// 查找坏字符所在的最后一个位置
			Integer bcid = bc.get(bad);
			int badIdx = bcid != null ? bcid : -1;
			// 坏字符偏移量
			int badPos = idx - badIdx;
			int goodPos = 0;
			if (idx < m - 1) {// 有好字符
				goodPos = getGoodPos(idx, m, suffix, prefix);
			}
			// 得出更大的偏移量进行偏移
			int max = Math.max(badPos, goodPos);
			pos = pos + max;
		}

		return -1;
	}

	///////////////////////////////////
	// 以下为静态方法部分
	//////////////////////////////////
	/**
	 * bm算法 全部
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static LinkedList<Integer> bmAll(String text, String match) {
		return bmAll(text.toCharArray(), match.toCharArray(), 0);
	}

	/**
	 * bm算法
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static int bm(String text, String match) {
		return bm(text.toCharArray(), match.toCharArray(), 0);
	}

	/**
	 * bm算法 查全部 带偏移量
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static LinkedList<Integer> bmAll(String text, String match, int pos) {
		return bmAll(text.toCharArray(), match.toCharArray(), pos);
	}

	/**
	 * bm算法 带偏移量
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static int bm(String text, String match, int pos) {
		return bm(text.toCharArray(), match.toCharArray(), pos);
	}

	/**
	 * bm算法 匹配全部
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static LinkedList<Integer> bmAll(char[] a, char[] b, int pos) {
		// int pos = 0;// 主串位置偏移量
		LinkedList<Integer> res = new LinkedList<Integer>();
		int n = a.length;
		int m = b.length;
		HashMap<Character, Integer> bc = buildBc(b, m);// 坏字符字典
		boolean[] prefix = new boolean[b.length];// 好前缀字典
		int[] suffix = new int[b.length];// 好字符字典
		buildGc(b, m, suffix, prefix);// 号支付构建
		while (pos <= n - m) {// 偏移量未到底的时候
			int idx = m - 1;// 当前模式串的最后坐标
			for (; idx >= 0; idx--) {
				// 模式串和主串的末尾部分相不匹配的时候
				if (b[idx] != a[idx + pos]) {
					break;
				}
			}
			if (idx < 0) {// 匹配
				res.add(pos);
				idx++;// 这里当成可匹配字符减1 即从下一个字符开始匹配
				// return pos;
			}

			char bad = a[pos + idx];// 坏字符
			// int badIndex = bad;// 坏字符字典索引
			// 查找坏字符所在的最后一个位置
			Integer bcid = bc.get(bad);
			int badIdx = bcid != null ? bcid : -1;
			// 坏字符偏移量
			int badPos = idx - badIdx;
			int goodPos = 0;
			if (idx < m - 1) {// 有好字符
				goodPos = getGoodPos(idx, m, suffix, prefix);
			}
			// 得出更大的偏移量进行偏移
			int max = Math.max(badPos, goodPos);
			pos = pos + max;
		}

		return res;
	}

	/**
	 * bm算法
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static int bm(char[] a, char[] b, int pos) {
		// int pos = 0;// 偏移量
		int n = a.length;
		int m = b.length;
		HashMap<Character, Integer> bc = buildBc(b, m);// 坏字符字典
		boolean[] prefix = new boolean[b.length];// 好前缀字典
		int[] suffix = new int[b.length];// 好字符字典
		buildGc(b, m, suffix, prefix);// 号支付构建
		while (pos <= n - m) {// 偏移量未到底的时候
			int idx = m - 1;// 当前模式串的最后坐标
			for (; idx >= 0; idx--) {
				// 模式串和主串的末尾部分相不匹配的时候
				if (b[idx] != a[idx + pos]) {
					break;
				}
			}
			if (idx < 0) {// 匹配
				return pos;
			}

			char bad = a[pos + idx];// 坏字符
			// int badIndex = bad;// 坏字符字典索引
			// 查找坏字符所在的最后一个位置
			Integer bcid = bc.get(bad);
			int badIdx = bcid != null ? bcid : -1;
			// 坏字符偏移量
			int badPos = idx - badIdx;
			int goodPos = 0;
			if (idx < m - 1) {// 有好字符
				goodPos = getGoodPos(idx, m, suffix, prefix);
			}
			// 得出更大的偏移量进行偏移
			int max = Math.max(badPos, goodPos);
			pos = pos + max;
		}

		return -1;
	}

	/**
	 * 获得好字符的偏移量
	 * 
	 * @author lfy
	 * @param j
	 * @param m
	 * @param suffix
	 * @param prefix
	 * @return
	 */
	private static int getGoodPos(int j, int m, int[] suffix, boolean[] prefix) {
		int l = m - 1 - j;// 好字符长度
		if (suffix[l] != -1) {// 匹配到的时候
			// 如 好字符长度为2 总长度为5 应该偏移长度为 3
			// 因为suffix保存的是下标 所以 应该计算长度时候应该加1
			int len = j - suffix[l] + 1;
			return len;
		}
		// 后缀无法匹配则匹配前缀
		for (int i = l; i > 0; i--) {
			// 前缀长度会进行递减 直到匹配为止
			// 如好字符长度为2，总长度5 也是正确的前缀
			// 即i=2: 5+1-2 = 4
			if (prefix[i]) {
				return j - suffix[l] + 1;

			}
		}
		// 完全不匹配 直接跳跃整个字符长度
		return m;
	}

	/**
	 * 构建坏字典
	 * 
	 * @author lfy
	 * @param a
	 * @param l
	 * @return
	 */
	private static HashMap<Character, Integer> buildBc(char[] a, int l) {
		HashMap<Character, Integer> bc = new HashMap<Character, Integer>();// ASCII码和坐标对应的字典
		// 默认值为null
		// 设置坐标
		for (int i = 0; i < l; i++) {
			bc.put(a[i], i);
		}
		return bc;
	}

	/**
	 * 构建好字典
	 * 
	 * @author lfy
	 * @param b
	 * @param m
	 * @param suffix
	 * @param prefix
	 */
	private static void buildGc(char[] b, int m, int[] suffix, boolean[] prefix) {
		// 初始化
		for (int i = 0; i < m; i++) {
			suffix[i] = -1;
			prefix[i] = false;
		}
		for (int i = 0; i < m; i++) {
			int p = i;// 前坐标
			int s = m - 1;// 后坐标
			while (p >= 0 && s > p && b[p] == b[s]) {
				// 每次都从末尾和当前字符匹配 匹配成功了 当前字符和末尾字符一起向前一位再进行匹配
				suffix[m - s] = p;
				s--;
				p--;
			}
			if (p == -1) {
				// 匹配到了起始位置
				// 即通过循环计算后发现 (m - 1 - s)~(m - 1)个字符和字符串前缀一样
				prefix[m - 1 - s] = true;
			}
		}

	}

	@Override
	public String toString() {
		return "BMSearch [match=" + new String(match) + "]";
	}
	
}
