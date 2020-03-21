package com.lahuan.webcrawler.duplicationfilter;


/**
 * 布隆过滤器工具
 * 
 * @author la-huan
 *
 */
public class BloomFilter implements URLFilter {
	private BitMap bitmap;// 位图
	private BloomHash[] hashs = new BloomHash[5];// 固定5个hash算法

	@Deprecated // 不打算开放出去
	BloomFilter() {
		// 位图大小设置为Integer.MAX_VALUE 即0和正数都包含
		// 在这里我曾经纠结bitmap这样设置是否能够存放第Integer.MAX_VALUE个数据
		// 后面debug后发现 Integer.MAX_VALUE除以32是不能整除的(余数31),因为Integer.max = 2^31-1.
		// 因此刚好有一个位置的冗余，可以存放第Integer.MAX_VALUE个数据
		bitmap = new BitMapMemoryImpl(Integer.MAX_VALUE);
		// 固定5个哈希算法
		hashs[0] = new JavaHash();
		hashs[1] = new Md5Hash();
		hashs[2] = new SimpleHash(1);
		hashs[3] = new Md5Hash("salt");
		hashs[4] = new SimpleHash(2);
	}

	public BloomFilter(BitMap bitmap, BloomHash[] hashs) {
		super();
		this.bitmap = bitmap;
		this.hashs = hashs;
	}

	public boolean contains(String url) {
		if (url == null) {
			return false;
		}
		for (BloomHash f : hashs) {
			if (!bitmap.get(f.hash(url))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean filter(String url) {
		// CSS和JS文件等直接跳过
		if (url.endsWith(".css") || url.endsWith(".js")) {
			// JS 和css文件
			return false;
		}
		// 计算所有的hash
		int h = 0;
		boolean res = true;
		for (int i = 0; i < hashs.length; i++) {
			h = hashs[i].hash(url);
			res = res && bitmap.get(h);
			// 直接设置为true
			// 如果是不通过的,本身这几个就都是true
			// 如果是通过的,应该设置为true
			bitmap.setTrue(h);
		}
		// 5个hash对应位置都是true,代表这个数据是重复的（有几率误判）
		// 但如果有一个false,就代表这个数据一定是没重复的（不会误判）
		return !res;
	}

}