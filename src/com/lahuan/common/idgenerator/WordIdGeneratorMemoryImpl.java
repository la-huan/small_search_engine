package com.lahuan.common.idgenerator;

import java.util.concurrent.atomic.AtomicLong;
/**
 * 实现
 * @author la-huan
 *
 */
public class WordIdGeneratorMemoryImpl implements WordIdGenerator{
	
	private  AtomicLong seed=new AtomicLong(0);
	
	public  Long getId(){
		long res = seed.getAndIncrement();
		return res;
	}
	
}
