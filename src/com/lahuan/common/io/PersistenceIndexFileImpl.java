package com.lahuan.common.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class PersistenceIndexFileImpl implements PersistenceIndex {
	public static void main(String[] args) throws Exception {
		PersistenceIndexFileImpl idx = new PersistenceIndexFileImpl("D:\\temp\\test.txt");
		for (int i = 1; i < 20; i++) {
			String s = i + "" + i + "" + i + "" + i + "" + i;
			long offset = idx.write(s.getBytes());
			System.out.print(offset + ",");
		}
	}

	String fileName;
	RandomAccessFile rdFile;
	FileChannel channel;

	public PersistenceIndexFileImpl(String fileName) {
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
	public long write(byte[] bytes) {
		long r = -1;
		try {
			MappedByteBuffer bbf = channel.map(MapMode.READ_WRITE, rdFile.length(), bytes.length);
			bbf.put(bytes);
			r = rdFile.length();
			;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return r;
	}

	@Override
	public byte[] read(long s, long e) {
		byte[] bytes = null;
		try {
			bytes = new byte[(int) (e - s)];
			MappedByteBuffer bbf = channel.map(MapMode.READ_ONLY, s, bytes.length);
			bbf.get(bytes);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return bytes;
	}

}
