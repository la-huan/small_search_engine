package com.lahuan.common.config;

import java.io.File;

/**
 * 管理全局配置
 * 
 * @author la-huan
 *
 */
public class GlobelConfig {
	/**
	 * 设置文件路径 
	 * 
	 * @param path 文件夹路径
	 */
	public static void setFilePath(String path) {
		if(path==null||"".equals(path)){
			throw new  RuntimeException("文件路径错误");
		}
		File dir = new File(path);
		if(!dir.exists()){
			dir.mkdirs();
		}
		FILE_PATH = path;
	}

	/**
	 * 获取文件路径
	 * 
	 * @param path
	 */
	public static String getFilePath() {
		return FILE_PATH;
	}

	/**
	 * 获取持久化偏移量文件路径
	 * 
	 * @param path
	 */
	public static String getPersistenceOffsetFilePath() {
		return FILE_PATH + PERSISTENCE_FILE_OFFSET_FILE_NAME;
	}

	/**
	 * 获取持久化的索引文件路径
	 * 
	 * @param path
	 */
	public static String getPersistenceIdxFilePath() {
		return FILE_PATH + PERSISTENCE_FILE_INDEX_FILE_NAME;
	}

	/**
	 * 获取日志文件路径
	 * 
	 * @param path
	 */
	public static String getLogFilePath() {
		return FILE_PATH + DEFAULT_LOG_PATH_FILE_NAME;
	}

	/**
	 * 默认的路径
	 */
	private static String FILE_PATH = "temp";// "D:\\temp";
	/**
	 * 默认的日志文件
	 */
	private static final String DEFAULT_LOG_PATH_FILE_NAME = "\\log\\demo_search_engine.log";
	/**
	 * 持久化 偏移量文件名
	 */
	private static final String PERSISTENCE_FILE_OFFSET_FILE_NAME = "\\PERSISTENCE_OFF.bin";
	/**
	 * 持久化 索引文件名
	 */
	private static final String PERSISTENCE_FILE_INDEX_FILE_NAME = "\\PERSISTENCE_IDX.bin";
	
	/**
	 * 首页HTML文件
	 */
	public static final String SERVER_FILE_INDEX = "html/index.html";
	/**
	 * 搜索结果HTML文件前缀
	 */
	public static final String SERVER_FILE_RESULT_PREFIX = "html/result_prefix.txt";
	/**
	 * 搜索结果HTML文件后缀
	 */
	public static final String SERVER_FILE_RESULT_SUFFIX = "html/result_suffix.txt";
	/**
	 * 默认的词库路径
	 */
	public static final String DEFAULT_WORDS_FILE_PATH = "words";
	/**
	 * 格式化后的html的key
	 */
	public static final String ANALYSE_FORMAT_HTML_KEY = "ANALYSE_FORMAT_HTML_KEY";
	/**
	 * 默认的html编码
	 */
	public static final String DEFAULT_HTML_CHARSET = "utf-8";

	/**
	 * 分析队列的默认分隔符
	 */
	public static final String ANALYSE_QUEUE_SEPARATE = "==SEPARATE_JEvYgHAs_6reIN6zj==";// 随机字符串
	/**
	 * 在格式化时候，分隔title、description等分隔符
	 */
	public static final String CONTENT_FORMAT_SEPARATE = "-=iZ2jS4Ie-=";// 随机字符串
	/**
	 * 文件队列每次最大获取数量默认值
	 */
	public static final int QUEUE_FILE_PER_MAX = 10;
	/**
	 * 各个线程每次检查状态最大等待毫秒
	 */
	public static final long THREAD_CHECK_LOOP_PER_MS = 10 * 1000L;

	/**
	 * 分析队列名
	 */
	public static final String QUEUE_ANALYSE_NAME = "QUEUE_ANALYSE";
	/**
	 * URL队列名
	 */
	public static final String QUEUE_LINKS_NAME = "QUEUE_LINKS";
	/**
	 * URL的ID名
	 */
	public static final String QUEUE_URL_ID_NAME = "QUEUE_URL_ID";

	/**
	 * 监控线程 队列大小阈值
	 */
	public static final long MONITOR_THREAD_THRESHOLD = 300;
	////////////////////////
	// 全局上下文里的key
	////////////////////////
	/**
	 * 分析队列名的key
	 */
	public static final String QUEUE_ANALYSE_KEY = "QUEUE_ANALYSE_KEY";
	/**
	 * URL队列名的KEY
	 */
	public static final String QUEUE_LINKS_KEY = "QUEUE_LINKS_KEY";
	/**
	 * URL的ID名的key
	 */
	public static final String QUEUE_URL_ID_KEY = "QUEUE_URL_ID_KEY";
	/**
	 * URL爬虫过滤器的的key
	 */
	public static final String WEBCRAWLER_FILTER_KEY = "WEBCRAWLER_FILTER_KEY";
	/**
	 * 爬虫调用链的key
	 */
	public static final String WEBCRAWLER_CHAIN_KEY = "WEBCRAWLER_CHAIN_KEY";
	/**
	 * 爬虫线程
	 */
	public static final String WEBCRAWLER_THREAD_KEY = "WEBCRAWLER_THREAD_KEY";
	/**
	 * 爬虫线程数量
	 */
	public static final int WEBCRAWLER_THREAD_SIZE = 1;
	/**
	 * 分析线程数量
	 */
	public static final int ANALYSE_THREAD_SIZE = 1;
	/**
	 * 全部词库的AC自动机
	 */
	public static final String GLOBEL_WORDS_AUTOMATON = "GLOBEL_WORDS_AUTOMATON";

	/**
	 * 分析调用链的key
	 */
	public static final String ANALYSE_CHAIN_KEY = "ANALYSE_CHAIN_KEY";
	/**
	 * 倒排索引用的map
	 */
	public static final String ANALYSE_TMP_IDX_MAP_KEY = "ANALYSE_IDX_MAP_KEY";
	/**
	 * 分析线程
	 */
	public static final String ANALYSE_THREAD_KEY = "ANALYSE_THREAD_KEY";

	/**
	 * 分析持久化线程
	 */
	public static final String ANALYSE_PERSISTENCE_THREAD_KEY = "ANALYSE_PERSISTENCE_THREAD_KEY";

	/**
	 * 持久化 偏移量文件名的KEY
	 */
	public static final String PERSISTENCE_FILE_OFFSET_KEY = "PERSISTENCE_OFF_KEY";
	/**
	 * 持久化 索引文件名的KEY
	 */
	public static final String PERSISTENCE_FILE_INDEX_KEY = "PERSISTENCE_IDX_KEY";

	/**
	 * 分析持久化线程
	 */
	public static final String MONITOR_THREAD_KEY = "MONITOR_THREAD_KEY";
	/**
	 * 词库查找器的KEY
	 */
	public static final String QUERY_WORDS_SEARCHER_KEY = "QUERY_WORDS_SEARCHER_KEY";
	/**
	 * URL查找器的KEY
	 */
	public static final String QUERY_URL_SEARCHER_KEY = "QUERY_URL_SEARCHER_KEY";
	
	////////////////////////////////服务器
	/**
	 * 服务器处理集合
	 */
	public static final String SERVER_HANDLERS_LIST = "SERVER_HANDLERS_LIST_KEY";
	/**
	 * 服务器默认处理
	 */
	public static final String SERVER_DEFAULT_HANDLER = "SERVER_DEFAULT_HANDLER_KEY";
	/**
	 * 服务器对象
	 */
	public static final String SERVER_OBJECT = "SERVER_OBJECT_KEY";

}
