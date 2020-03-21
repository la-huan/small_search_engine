package com.lahuan.common.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileWriteUtil {


	String fileName;
	RandomAccessFile rdFile;
	FileChannel channel;
	/**
	 * 初始化
	 * @param fileName
	 */
	public FileWriteUtil(String fileName) {
		try {
			this.fileName = fileName;
			File file = new File(fileName);
			if (!file.exists()) {
				try {
					if(!file.getParentFile().exists()){
						file.getParentFile().mkdirs();
					}
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			rdFile = new RandomAccessFile(file, "rw");
			channel = rdFile.getChannel();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 写入 返回值为文件的offset
	 * 
	 * @param line
	 * @return
	 * @throws IOException
	 */
	public long write(byte[] bytes) {
		long r = -1;
		try {
			MappedByteBuffer bbf = channel.map(MapMode.READ_WRITE, rdFile.length(), bytes.length);
			bbf.put(bytes);
			r = rdFile.length();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return r;
	}

}
