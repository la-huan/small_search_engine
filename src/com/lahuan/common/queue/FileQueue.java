package com.lahuan.common.queue;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;

/**
 * 文件队列
 * 不支持并发 并发会出现各种内存映射错误
 * @author la-huan
 *
 */
public class FileQueue {

	public static void main(String[] args) throws Exception {
		String base = "阿萨德执行称其为地方";
		FileQueue queue = new FileQueue("temp","test");
		long min = queue.getQueueSize();
		long size = min + 5;
		for (long i = min; i < size; i++) {
			byte[] b = (base + i).getBytes();
			queue.write(b);
		}
		//queue.rewind();
		size = queue.getLeftNum();
		// size = queue.getQueueSize();
		//size =0;
		for (int i = 0; i < size; i++) {
			// byte[] bytes = queue.randomGet(i);
			byte[] bytes = queue.seqGet();
			System.out.println(Arrays.toString(bytes));
			if (bytes != null)
				System.out.println(new String(bytes));
			System.out.println("------");
		}
	}
	
	
	// 每页大小 每次映射文件的大小 不能大于INT_MAX 不能小于16 并且必须是16的倍数
	private static final int PAGE_SIZE =  4 * 1024 ;
	// LONG的大小 即每个地址的大小
	private static final int LONG_SIZE = Long.BYTES;// 8
	// 队列路径
	private String fileDir = null;// 默认磁盘路径
	// 队列名，即文件名，暂时不考虑分文件保存。
	private String queueName = fileDir + "\\default";
	// 采用byte数组保存主数据
	private String fileName = queueName + "_dat.bin";
	// 每两个long为一组保存
	// 保存每个数据对象的起始位置和结束位置
	private String indexName = queueName + "_idx.bin";
	// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
	private String pointName = queueName + "_pt.bin";
	//////////////////////
	// 文件随机访问对象
	//////////////////////
	/**
	 * 读文件
	 */
	private RandomAccessFile readFile;
	/**
	 * 写文件
	 */
	private RandomAccessFile writeFile;
	/**
	 * 写索引文件
	 */
	private RandomAccessFile writeIndexFile;
	/**
	 * 读索引文件
	 */
	private RandomAccessFile readIndexFile;
	/**
	 * 指针文件
	 */
	private RandomAccessFile pointFile;
	//////////////////////
	// 通道
	//////////////////////
	/**
	 * 读文件通道
	 */
	private FileChannel readChannel;
	/**
	 * 写文件通道
	 */
	private FileChannel writeChannel;

	/**
	 * 写索引文件通道
	 */
	private FileChannel writeIndexChannel;
	/**
	 * 读索引文件通道
	 */
	private FileChannel readIndexChannel;
	/**
	 * 指针文件通道
	 */
	private FileChannel pointChannel;
	//////////////////////
	// 内存映射文件
	//////////////////////
	/**
	 * 内存映射文件 读文件
	 */
	private MappedByteBuffer readBuff;
	/**
	 * 内存映射文件 写文件
	 */
	private MappedByteBuffer writeBuff;
	/**
	 * 内存映射文件 写索引
	 */
	private MappedByteBuffer writeIndexBuff;
	/**
	 * 内存映射文件 读索引
	 */
	private MappedByteBuffer readIndexBuff;
	/**
	 * 内存映射文件 指针
	 */
	private MappedByteBuffer pointBuff;

	/**
	 * 队列大小
	 */
	private long queueSize = 0;
	/**
	 * 读指针
	 */
	private long readIdx = 0;
	/**
	 * 写指针
	 */
	private long writeIdx = 0;
	/**
	 * 索引指针
	 */
	private long indexIdx = 0;

	/**
	 * 磁盘路径 队列名
	 * 
	 * @param fileDir
	 * @param queueName
	 */
	public FileQueue(String fileDir, String queueName) {
		this.fileDir = fileDir;
		init(queueName);
	}

	

	/**
	 * 初始化
	 * 
	 * @param queueName
	 */
	private void init(String queueName) {
		this.queueName = queueName;
		queueName = fileDir + "\\" + queueName;
		// 采用byte数组保存数据
		fileName = queueName + "_dat.bin";
		// 每两个long为一组保存
		// 保存每个数据对象的起始位置和结束位置
		indexName = queueName + "_idx.bin";
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointName = queueName + "_pt.bin";
		try {
			// 初始化文件
			initFile();
			// 初始化指针
			initPoint();
			System.out.println("Queue init success:"+queueName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读指针重置 可以重新顺序读取
	 * 
	 * @throws Exception
	 */
	public void rewind() throws Exception {
		setReadIdx(0);
		readBuff = readChannel.map(MapMode.READ_WRITE, 0, PAGE_SIZE);
		readIndexBuff = readIndexChannel.map(MapMode.READ_WRITE, 0, PAGE_SIZE);
	}

	/**
	 * 顺序读取 自动记录读指针
	 * 
	 * @return
	 * @throws Exception
	 */
	public byte[] seqGet() throws Exception {
		if (readIdx == queueSize) {
			return null;// 读取到末尾了
		}
		// 先计算索引偏移量
		long offset = readIdx * 2 * LONG_SIZE;
		// 计算偏移量
		long s1 = offset % PAGE_SIZE;
		readIndexBuff.position((int) s1);
		// 起始位置和结束位置
		long s = readIndexBuff.getLong();
		long e = readIndexBuff.getLong();
		byte[] b = seqRead(s, e);
		// 判断写是否需要进行翻页
		//计算新的位置
		offset = offset + 2 * LONG_SIZE;
		s1 = offset % PAGE_SIZE;
		if (s1==0) {
			// 计算页码
			long page = offset / PAGE_SIZE;
			readIndexBuff = writeIndexChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
		}
		setReadIdx(readIdx + 1);
		return b;
	}

	/**
	 * 顺序读
	 * 
	 * @param s
	 * @param e
	 * @return
	 * @throws Exception
	 */
	private byte[] seqRead(long s, long e) throws Exception {
		int size = (int) (e - s);
		int readPos = 0;// 数组偏移量
		byte[] r = new byte[size];
		// 当前映射偏移量
		long pos = s % PAGE_SIZE;
		// 当前映射剩余空间
		long left = PAGE_SIZE - pos;
		while (readPos < r.length) {
			long length = left > size ? size : left;
			readBuff.position((int) pos);
			readBuff.get(r, readPos, (int) length);
			//
			readPos += length;
			size -= length;
			left -= length;
			// 判断是否需要跳到下一个映射
			if (left == 0) {
				long page = (s + readPos) / PAGE_SIZE;
				readBuff = readChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
				pos = 0;
				left = PAGE_SIZE;
			}

		}

		return r;
	}

	/**
	 * 随机读指定位置的数据  每次都新建个独立的文件映射
	 * 
	 * @param idx
	 * @return
	 * @throws Exception
	 */
	public byte[] randomGet(long idx) throws Exception {
		if (idx >= queueSize) {
			throw new RuntimeException("随机访问错误:队列大小:" + queueSize + ",索引位置:" + idx);
		}
		// 先计算索引偏移量
		long offset = idx * 2 * LONG_SIZE;
		// 计算偏移量
		long s1 = offset % PAGE_SIZE;
		// 计算页码
		long page = offset / PAGE_SIZE;
		MappedByteBuffer indexBuff = readIndexChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
		indexBuff.position((int) s1);
		// 起始位置和结束位置
		long s = indexBuff.getLong();
		long e = indexBuff.getLong();

		return randomRead(s, e);
	}

	/**
	 * 随机读取
	 * 
	 * @param s
	 * @param e
	 * @return
	 * @throws Exception
	 */
	private byte[] randomRead(long s, long e) throws Exception {
		int size = (int) (e - s);
		int readPos = 0;// 数组偏移量
		byte[] r = new byte[size];
		// long start =s;
		long page = s / PAGE_SIZE;
		MappedByteBuffer randomReadBuff = readChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
		// 当前映射偏移量
		long pos = s % PAGE_SIZE;
		// 当前映射剩余空间
		long left = PAGE_SIZE - pos;
		while (readPos < r.length) {
			long length = left > size ? size : left;
			randomReadBuff.position((int) pos);
			randomReadBuff.get(r, readPos, (int) length);
			//
			readPos += length;
			size -= length;
			left -= length;
			// 判断是否需要跳到下一个映射位置
			if (left == 0 && r.length > readPos) {
				randomReadBuff = readChannel.map(MapMode.READ_WRITE, ++page * PAGE_SIZE, PAGE_SIZE);
				pos = 0;
				left = PAGE_SIZE;
			}

		}
		return r;
	}

	/**
	 * 写入
	 * 会返回队列大小
	 * @param b
	 * @throws Exception
	 */
	public long write(byte[] b) throws Exception {
		// 起始位置和大小
		long start = writeIdx;
		long size = b.length;
		// 文件偏移量
		long s1 = start % PAGE_SIZE;
		// 当前页剩余多少
		long left = PAGE_SIZE - s1;
		// byte数组偏移量
		long pos = 0;
		// 写入数据文件
		// 需要跨页写入的时候 循环调用
		while (size > 0) {
			// 获取当前页写入的起始位置
			// 计算当前页剩余多少
			// 最大能放的长度
			long length = left > size ? size : left;
			writeBuff.position((int) s1);
			writeBuff.put(b, (int) pos, (int) length);
			// 指针重设
			pos += length;
			start += length;
			size -= length;
			// 本次完成 判断是否进行跨页
			s1 = start % PAGE_SIZE;// 偏移量
			if (s1 == 0) {
				// 本页已满的时候， 这里必须保证传入的byte数组 size大于0 否则这里的判断是错误的
				// 计算旧页码
				long page = start / PAGE_SIZE;
				// 调整页码
				writeBuff = writeChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
				// s1归零
				// s1 = 0;
			}
			left = PAGE_SIZE - s1;
		}
		// 写入索引文件
		writeIndex(writeIdx, writeIdx + b.length);
		// 更新指针文件
		setWriteIdx(writeIdx + b.length);
		// 队列大小增加
		setQueueSize(queueSize + 1);
		return queueSize;
	}

	/**
	 * 写入索引 起始位置和结束位置
	 * 
	 * @param s
	 * @param e
	 * @throws Exception
	 */
	private void writeIndex(long s, long e) throws Exception {
		long s1 = indexIdx % PAGE_SIZE;
		writeIndexBuff.position((int) s1);
		writeIndexBuff.putLong(s);
		writeIndexBuff.putLong(e);
		// 设置索引指针
		setIndexIdx(indexIdx + LONG_SIZE * 2);
		s1 = indexIdx % PAGE_SIZE;
		if (s1 == 0) {
			// 需要切换新的页了
			long page = indexIdx / PAGE_SIZE;
			// 调整页码
			writeIndexBuff = writeIndexChannel.map(MapMode.READ_WRITE, page * PAGE_SIZE, PAGE_SIZE);
		}

	}

	/**
	 * 初始化文件
	 * 
	 * @throws Exception
	 */
	private void initFile() throws Exception {
		// 文件 这里会自动创建文件
		readFile = new RandomAccessFile(fileName, "rw");
		writeFile = new RandomAccessFile(fileName, "rw");
		writeIndexFile = new RandomAccessFile(indexName, "rw");
		readIndexFile = new RandomAccessFile(indexName, "rw");
		pointFile = new RandomAccessFile(pointName, "rw");
		// 通道
		readChannel = readFile.getChannel();
		writeChannel = writeFile.getChannel();
		writeIndexChannel = writeIndexFile.getChannel();
		readIndexChannel = readIndexFile.getChannel();
		pointChannel = pointFile.getChannel();

	}

	/**
	 * 初始化指针
	 * 
	 * @throws Exception
	 */
	private void initPoint() throws Exception {
		// 指针文件映射
		pointBuff = pointChannel.map(FileChannel.MapMode.READ_WRITE, 0, Long.BYTES * 4);
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointBuff.rewind();
		queueSize = pointBuff.getLong();
		indexIdx = pointBuff.getLong();
		writeIdx = pointBuff.getLong();
		readIdx = pointBuff.getLong();
		// 内存映射文件 这里会自动扩大文件
		// FileChannel.MapMode.READ_WRITE
		// 初始化读写指针
		initWritePoint();
		initReadPoint();
	}

	/**
	 * 初始化写指针
	 * 
	 * @throws Exception
	 */
	private void initWritePoint() throws Exception {
		// 顺序写文件映射
		long pageWrite = writeIdx / PAGE_SIZE;// 页码
		writeBuff = writeChannel.map(MapMode.READ_WRITE, pageWrite * PAGE_SIZE, PAGE_SIZE);

		// 写索引文件映射
		long pageWriteIdx = indexIdx / PAGE_SIZE;// 页码
		writeIndexBuff = writeIndexChannel.map(MapMode.READ_WRITE, pageWriteIdx * PAGE_SIZE, PAGE_SIZE);

	}

	/**
	 * 初始化读指针
	 * 
	 * @throws Exception
	 */
	private void initReadPoint() throws Exception {
		if (readIdx == 0) {
			// 未读取的时候 直接从头开始
			readIndexBuff = readIndexChannel.map(MapMode.READ_WRITE, 0, PAGE_SIZE);
			readBuff = readChannel.map(MapMode.READ_WRITE, 0, PAGE_SIZE);
		} else {

			// 获取读索引文件 前一个映射
			long readOffset = (readIdx - 1) * 2 * LONG_SIZE;
			long pageReadIdx = readOffset / PAGE_SIZE;// 页码
			readIndexBuff = readIndexChannel.map(MapMode.READ_WRITE, pageReadIdx * PAGE_SIZE, PAGE_SIZE);

			// 顺序读文件映射
			// 根据前一个读索引获取读指针应该在的位置
			long s1 = readOffset % PAGE_SIZE;
			readIndexBuff.position((int) s1);
			// 起始位置
			readIndexBuff.getLong();
			long e = readIndexBuff.getLong();
			long pageRead = e / PAGE_SIZE;// 页码
			readBuff = readChannel.map(MapMode.READ_WRITE, pageRead * PAGE_SIZE, PAGE_SIZE);
			if (2 * LONG_SIZE + s1 == PAGE_SIZE) {
				// 因为读索引读取的是前一个的 如果当前索引页读取到底了 就需要切换到下一页
				readIndexBuff = readIndexChannel.map(MapMode.READ_WRITE, ++pageReadIdx * PAGE_SIZE, PAGE_SIZE);
			}
		}
	}

	/**
	 * 清空队列<br>
	 * 这里只是重设指针,没有真正的删除文件
	 */
	public void clear() {
		setQueueSize(0);
		setIndexIdx(0);
		setReadIdx(0);
		setWriteIdx(0);
	}

	/**
	 * 设置队列大小
	 * 
	 * @param v
	 */
	private void setQueueSize(long v) {
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointBuff.position(0);
		pointBuff.putLong(v);
		this.queueSize = v;
	}

	/**
	 * 设置写索引指针索引位置
	 * 
	 * @param v
	 */
	private void setIndexIdx(long v) {
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointBuff.position(LONG_SIZE);
		pointBuff.putLong(v);
		this.indexIdx = v;

	}

	/**
	 * 设置顺序写指针索引位置
	 * 
	 * @param v
	 */
	private void setWriteIdx(long v) {
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointBuff.position(LONG_SIZE * 2);
		pointBuff.putLong(v);
		this.writeIdx = v;
	}

	/**
	 * 设置顺序读指针索引位置
	 * 
	 * @param v
	 */
	private void setReadIdx(long v) {
		// 第0个保存队列大小 第1个保存写索引指针 第2个long保存写指针 第3个long保存读指针
		pointBuff.position(LONG_SIZE * 3);
		pointBuff.putLong(v);
		this.readIdx = v;
	}

	/**
	 * 获取未消费的数据数量
	 * 
	 * @return
	 */
	public long getLeftNum() {
		return queueSize - readIdx;
	}

	/**
	 * 队列总大小 包含已消费的
	 * 
	 * @return
	 */
	public long getQueueSize() {
		return queueSize;
	}

	/**
	 * 获取顺序读指针的位置
	 * 
	 * @return
	 */
	public long getReadIdx() {
		return readIdx;
	}

	/**
	 * 获取顺序写指针的位置
	 * 
	 * @return
	 */
	public long getWriteIdx() {
		return writeIdx;
	}

	/**
	 * 获取索引指针的位置
	 * 
	 * @return
	 */
	public long getIndexIdx() {
		return indexIdx;
	}

}
