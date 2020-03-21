package com.lahuan.webcrawler.duplicationfilter;

/**
 * 位图
 * 
 * @author la-huan
 *
 */
public interface BitMap {
	/**
	 * 设置位图大小
	 * @param s
	 */
	public void setSize(int s);
	/**
	 * 设置为某位true
	 * @param idx
	 */
	public void setTrue(int idx);
	/**
	 * 设置为某位false
	 * @param idx
	 */
	public void setFalse(int idx);
	/**
	 * 获取某位
	 * @param idx
	 */
	public boolean get(int idx);
}
