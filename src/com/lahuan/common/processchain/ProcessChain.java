package com.lahuan.common.processchain;

import com.lahuan.common.env.webcrawler.ProcessChainContext;

public interface ProcessChain {
	
	/**
	 * 调用链
	 * @return 返回false代表结束调用链
	 */
	boolean exec(ProcessChainContext ctx);
	
}
