package com.lahuan.webcrawler.duplicationfilter;

/**
 * 位图的内存实现 辅助给布隆过滤器使用
 * 
 * @author la-huan
 *
 */
public class BitMapMemoryImpl implements BitMap {
	private int[] bytes;// c
	private int size = 0;

	public BitMapMemoryImpl(int size) {
		if (size <= 0)
			throw new UnsupportedOperationException("size不可以小于等于0");
		this.size = size;
		bytes = new int[size / 32 + 1];
	}

	public BitMapMemoryImpl() {
	}

	public int getSize() {
		return size;
	}
	// public void print() {
	// for (int i = 0; i < size; i++) {
	// System.out.print((get(i) ? "1" : "0") + ",");
	// }
	// System.out.println();
	// }

	public void setTrue(int idx) {
		int arrIdx = idx / 32;// 数组索引
		int byteIdx = idx % 32;// byte索引
		// 二进制的或计算
		bytes[arrIdx] |= (1 << byteIdx);
	}

	public void setFalse(int idx) {
		int arrIdx = idx / 32;// 数组索引
		int byteIdx = idx % 32;// byte索引
		// 索引反码 然后与计算
		bytes[arrIdx] &= (~(1 << byteIdx));
	}

	public boolean get(int idx) {
		int arrIdx = idx / 32;// 数组索引
		int byteIdx = idx % 32;// byte索引
		// 二进制的与计算
		// (1 << byteIdx))的二进制只有byteIdx位置会是1 其它都是0
		// 所以结果如果为0 就是false 其它情况则是true
		return (bytes[arrIdx] & (1 << byteIdx)) != 0;

	}

	@Override
	public void setSize(int size) {
		if (size != 0)
			throw new UnsupportedOperationException("size只可以构造一次");
		this.size = size;
		bytes = new int[size / 32 + 1];
	}

}