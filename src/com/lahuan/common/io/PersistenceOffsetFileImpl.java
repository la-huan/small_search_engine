package com.lahuan.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class PersistenceOffsetFileImpl implements PersistenceOffset {
	String fileName;
	RandomAccessFile rdFile;
	FileChannel channel;
	int LONG_SIZE = Long.BYTES;

	public PersistenceOffsetFileImpl(String fileName) {
		try {
			this.fileName = fileName;
			File file = new File(fileName);
			if (!file.exists()) {
				try {
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
	public void write(Long s) {
		try {
			MappedByteBuffer bf = channel.map(MapMode.READ_WRITE, rdFile.length(), Long.BYTES);
			bf.putLong(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}
	/**
	 * 根据索引值获取偏移量
	 * 返回值为 long[2],0代表开始位置 1代表结束位置
	 * @param idx
	 * @return
	 */
	@Override
	public long[] readOffsetByIdx(long idx) {
		try {
			long start = idx * LONG_SIZE ;
			long[] r = new long[2];
			MappedByteBuffer bf = channel.map(MapMode.READ_ONLY, start, LONG_SIZE * 2);
			r[0] = bf.getLong();
			r[1] = bf.getLong();
			return r;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 获取索引大小
	 * @return
	 */
	@Override
	public int getSize() {
		try {
			return (int) (rdFile.length()/(LONG_SIZE)) - 1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	//最大索引
	@Override
	public int getMaxIdx() {
		try {
			return getSize()-1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}
