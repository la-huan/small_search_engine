package com.lahuan.common.search;

import java.util.LinkedList;
import java.util.List;

/**
 *KMP算法 
 * @author lfy
 */
public class KMPSearch {
	public static void main(String[] args) {

		KMPSearch k = new KMPSearch();
		Object r = k.kmpAll("bababa", "aba",4);
		System.out.println(r);

	}

	/**
	 * kmp查找全部索引 带偏移量
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public List<Integer> kmpAll(String text, String match, int pos) {
		return kmpAll(text.toCharArray(), match.toCharArray(), pos);
	}

	/**
	 * kmp查找全部索引
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public List<Integer> kmpAll(String text, String match) {
		return kmpAll(text.toCharArray(), match.toCharArray(), 0);
	}

	/**
	 * kmp查找 带偏移量
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public int kmp(String text, String match, int pos) {
		return kmp(text.toCharArray(), match.toCharArray(), pos);
	}

	/**
	 * kmp查找
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public int kmp(String text, String match) {
		return kmp(text.toCharArray(), match.toCharArray(), 0);
	}

	/**
	 * kmp查找
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static int kmp(char[] a, char[] b, int pos) {
		int n = a.length;
		int m = b.length;
		int[] next = getNexts(b, m);
		int j = 0;
		for (int i = pos; i < n; i++) {
			// 不匹配的时候 就去next数组里找
			while (j > 0 && a[i] != b[j]) {
				// 去next数组里找前一个字符所在的下标 进行匹配
				// 进行+1 是因为next数组里的 位置的转换
				// 即使用已构建好的失效函数
				j = next[j - 1] + 1;
			}
			// 当前字符匹配
			if (a[i] == b[j]) {
				j++;
			}
			// 完全匹配
			if (j == m) {
				// 当前坐标减模式串最大长度 即为首个字符出现的位置
				// 坐标从0开始 所以补个1
				return i - m + 1;

			}
		}

		return -1;// 不匹配
	}

	/**
	 * kmp查找全部索引
	 * 
	 * @author lfy
	 * @param a
	 * @param b
	 * @return
	 */
	public static List<Integer> kmpAll(char[] a, char[] b, int pos) {
		LinkedList<Integer> res = new LinkedList<Integer>();
		int n = a.length;
		int m = b.length;
		int[] next = getNexts(b, m);
		// System.out.println(Arrays.toString(next));
		int j = 0;
		for (int i = pos; i < n; i++) {
			// 不匹配的时候 就去next数组里找
			while (j > 0 && a[i] != b[j]) {
				// 去next数组里找前一个字符所在的下标 进行匹配
				// 进行+1 是因为next数组里的 位置的转换
				// 即使用已构建好的失效函数
				j = next[j - 1] + 1;
			}
			// 当前字符匹配
			if (a[i] == b[j]) {
				j++;
			}
			// 完全匹配
			if (j == m) {
				// 当前坐标减模式串最大长度 即为首个字符出现的位置
				// 坐标从0开始 所以补个1
				int r = i - m + 1;
				res.add(r);
				// 重置指针
				// 主串的指针 左移模式串长度-1个字符
				// 即主串返回当前匹配串的首个位置 然后再向右移动一位
				i = i - (m - 1);
				// 模式串归零
				j = 0;
				//
				// PS:这里想过用倒排失效指针的方法来一次性获取多个 但是写一半不合适
				// 例如多个指针指向同一个的时候 这时候就需要用数组/集合之类的保存 反而更多开销
				// 然后还需要做去重的判断 反而繁琐
				// PS2:突然想到 这里可以考虑 把BM算法的好词缀和坏词缀引入 当成从末尾到起始有(m-1)个字符匹配
				// 即首个字符不匹配的情况 需要额外空间再计算 不过如果这样 不如直接使用BM算法
			}
		}

		return res;// 不匹配
	}

	/**
	 * 获取失效函数
	 * 
	 * @author lfy
	 * @param b
	 * @param m
	 * @return
	 */
	private static int[] getNexts(char[] b, int m) {
		int[] n = new int[m];
		n[0] = -1;// 下标0 直接赋值 不需要计算
		int k = -1;// 下标
		// 动态规划
		for (int i = 1; i < m; i++) {
			while (k > -1 && b[k + 1] != b[i]) {
				// 不匹配的时候 用之前的结果 即使用已构建好的失效函数
				k = n[k];
			}
			// 匹配
			if (b[k + 1] == b[i]) {
				k++;// 相同 则i和k都累加
			}
			n[i] = k;
		}
		return n;
	}

}
