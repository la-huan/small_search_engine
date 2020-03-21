package com.lahuan.common.io;

import java.io.IOException;

public interface PersistenceIndex {

	/**
	 * 写入 返回值为文件的offset
	 * 
	 * @param line
	 * @return
	 * @throws IOException
	 */
	public long write(byte[] bytes);

	/**
	 * 根据起止位置返回
	 * 
	 * @param s
	 * @param e
	 * @return
	 */
	public byte[] read(long s, long e);
}
