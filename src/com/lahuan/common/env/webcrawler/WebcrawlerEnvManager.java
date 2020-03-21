package com.lahuan.common.env.webcrawler;

import java.util.LinkedList;
import java.util.List;

import com.lahuan.analyse.processchain.AnalyseBuildTempIndexChainExector;
import com.lahuan.analyse.processchain.AnalyseChainExector;
import com.lahuan.analyse.tempindex.TempIndexMap;
import com.lahuan.analyse.tempindex.TempIndexMemoryHashMap;
import com.lahuan.analyse.thread.AnalyseIndexThread;
import com.lahuan.analyse.thread.AnalysePersistenceThread;
import com.lahuan.common.config.GlobelConfig;
import com.lahuan.common.io.PersistenceIndex;
import com.lahuan.common.io.PersistenceIndexFileImpl;
import com.lahuan.common.io.PersistenceOffset;
import com.lahuan.common.io.PersistenceOffsetFileImpl;
import com.lahuan.common.queue.ConcurrentFileQueue;
import com.lahuan.common.queue.SearchEngineFileQueue;
import com.lahuan.common.queue.SearchEngineQueue;
import com.lahuan.common.search.ACAutomaton;
import com.lahuan.common.util.SelfLogger;
import com.lahuan.webcrawler.duplicationfilter.BitMap;
import com.lahuan.webcrawler.duplicationfilter.BitMapMemoryImpl;
import com.lahuan.webcrawler.duplicationfilter.BloomFilter;
import com.lahuan.webcrawler.duplicationfilter.BloomHash;
import com.lahuan.webcrawler.duplicationfilter.URLFilter;
import com.lahuan.webcrawler.duplicationfilter.JavaHash;
import com.lahuan.webcrawler.duplicationfilter.Md5Hash;
import com.lahuan.webcrawler.duplicationfilter.SimpleHash;
import com.lahuan.webcrawler.processchain.WebcrawlerAddAnalyseQueueExector;
import com.lahuan.webcrawler.processchain.WebcrawlerChainExector;
import com.lahuan.webcrawler.processchain.WebcrawlerFormatDocChainExector;
import com.lahuan.webcrawler.processchain.WebcrawlerHrefGetExector;
import com.lahuan.webcrawler.thread.WebcrawlerThread;

public class WebcrawlerEnvManager {
	public static void main(String[] args) {
		WebcrawlerEnvManager.init();
		WebcrawlerEnvManager.start();
	}

	// url队列
	private static SearchEngineQueue queueLinks;
	// 分析队列
	private static SearchEngineQueue queueAnalyse;
	// URL的ID队列
	private static ConcurrentFileQueue queueUrlId;
	// 根据词库创建的ac自动机
	private static ACAutomaton acAllWords;
	// 持久化索引
	private static PersistenceIndex persistenceidx;
	// 持久化偏移量
	private static PersistenceOffset persistenceOffset;
	// 爬虫线程
	private static WebcrawlerThread[] webcrawlerThreads;
	// 分析线程
	private static AnalyseIndexThread[] analyseIndexThreads;
	// 持久化倒排索引结果和偏移量
	private static AnalysePersistenceThread analysePersistenceThread;
	// 监控线程 负责线程状态的转换
	private static MonitorThreadForFIleQueue monitorThread;
	// 重复过滤器
	private static URLFilter filter;
	// 爬虫调用链
	private static List<WebcrawlerChainExector> chainsWebcrawler;
	// 倒排索引暂存的map
	private static TempIndexMap<Long, Long> tempIndexMap;
	// 分析调用链
	private static List<AnalyseChainExector> chainsAnalyse;

	/**
	 * 初始化
	 * 
	 * @param dir
	 *            存放索引队列等文件的路径
	 */
	public static void init(String dir) {
		//设置路径
		GlobelConfig.setFilePath(dir);
		initial();
	}
	/**
	 * 初始化
	 */
	public static void init() {
		// 保证创建文件夹
		GlobelConfig.setFilePath(GlobelConfig.getFilePath());
		initial();
	}

	/**
	 * 初始化
	 */
	private static void initial() {

		// 初始化日志
		SelfLogger.init(GlobelConfig.getLogFilePath());
		SelfLogger.log("Use file path:" + GlobelConfig.getFilePath());
		// url队列
		queueLinks = new SearchEngineFileQueue(GlobelConfig.getFilePath(), GlobelConfig.QUEUE_LINKS_NAME,
				GlobelConfig.QUEUE_FILE_PER_MAX);
		ProcessGlobelContext.put(GlobelConfig.QUEUE_LINKS_KEY, queueLinks);
		// 分析队列
		queueAnalyse = new SearchEngineFileQueue(GlobelConfig.getFilePath(), GlobelConfig.QUEUE_ANALYSE_NAME,
				GlobelConfig.QUEUE_FILE_PER_MAX);
		ProcessGlobelContext.put(GlobelConfig.QUEUE_ANALYSE_KEY, queueAnalyse);
		// URL的ID队列
		queueUrlId = new ConcurrentFileQueue(GlobelConfig.getFilePath(), GlobelConfig.QUEUE_URL_ID_NAME);
		ProcessGlobelContext.put(GlobelConfig.QUEUE_URL_ID_KEY, queueUrlId);
		// 重复过滤器
		filter = getFilter();
		ProcessGlobelContext.put(GlobelConfig.WEBCRAWLER_FILTER_KEY, filter);
		// 调用链
		chainsWebcrawler = getChainsWebcrawler(queueLinks, queueAnalyse, queueUrlId);
		ProcessGlobelContext.put(GlobelConfig.WEBCRAWLER_CHAIN_KEY, chainsWebcrawler);
		// 爬虫线程
		webcrawlerThreads = getWebcrawlerThread(GlobelConfig.WEBCRAWLER_THREAD_SIZE, queueLinks, chainsWebcrawler,
				filter);
		ProcessGlobelContext.put(GlobelConfig.WEBCRAWLER_THREAD_KEY, webcrawlerThreads);

		//////////////////////////////////////////////////////////////

		// 根据词库创建ac自动机
		acAllWords = ACAutomaton.getACAutomatonByWordsPath(GlobelConfig.DEFAULT_WORDS_FILE_PATH);
		ProcessGlobelContext.put(GlobelConfig.GLOBEL_WORDS_AUTOMATON, acAllWords);
		// 倒排索引暂存的map
		tempIndexMap = new TempIndexMemoryHashMap<Long, Long>();
		ProcessGlobelContext.put(GlobelConfig.ANALYSE_TMP_IDX_MAP_KEY, tempIndexMap);
		// 创建调用链
		chainsAnalyse = getChainsAnalyse(acAllWords, tempIndexMap);
		ProcessGlobelContext.put(GlobelConfig.ANALYSE_CHAIN_KEY, chainsAnalyse);
		// 分析线程
		analyseIndexThreads = getAnaylseIndexThread(GlobelConfig.ANALYSE_THREAD_SIZE, queueAnalyse, chainsAnalyse);
		ProcessGlobelContext.put(GlobelConfig.ANALYSE_THREAD_KEY, analyseIndexThreads);
		/////////////////////////////////////////////////////////////////
		// 持久化索引
		persistenceidx = new PersistenceIndexFileImpl(GlobelConfig.getPersistenceIdxFilePath());
		ProcessGlobelContext.put(GlobelConfig.PERSISTENCE_FILE_INDEX_KEY, persistenceidx);

		// 持久化偏移量
		persistenceOffset = new PersistenceOffsetFileImpl(GlobelConfig.getPersistenceOffsetFilePath());
		ProcessGlobelContext.put(GlobelConfig.PERSISTENCE_FILE_OFFSET_KEY, persistenceOffset);
		// 持久化倒排索引结果和偏移量
		analysePersistenceThread = new AnalysePersistenceThread(tempIndexMap, persistenceidx, persistenceOffset);
		ProcessGlobelContext.put(GlobelConfig.ANALYSE_PERSISTENCE_THREAD_KEY, analysePersistenceThread);
		// 监控线程 负责线程状态的转换
		monitorThread = new MonitorThreadForFIleQueue(GlobelConfig.MONITOR_THREAD_THRESHOLD,
				(SearchEngineFileQueue) queueAnalyse, (SearchEngineFileQueue) queueLinks, tempIndexMap);
		ProcessGlobelContext.put(GlobelConfig.MONITOR_THREAD_KEY, monitorThread);
		// 在URL队列里放入种子信息 重复放入也没事 有布隆过滤器 也不会重复爬取
		List<String> urlList = WebEntryEnum.getUrlList();
		for (String url : urlList) {
			queueLinks.send(url);
		}
		// startThread();
		// startMonitor();
	}

	/**
	 * 获取爬取阈值<br>
	 * 即 GlobelConfig.MONITOR_THREAD_THRESHOLD;
	 * 
	 * @return
	 */
	public static long getThreshold() {
		return GlobelConfig.MONITOR_THREAD_THRESHOLD;
	}

	private static AnalyseIndexThread[] getAnaylseIndexThread(int size, SearchEngineQueue queueAnalyse,
			List<AnalyseChainExector> chainsAnalyse) {
		AnalyseIndexThread[] r = new AnalyseIndexThread[size];
		for (int i = 0; i < r.length; i++) {
			r[i] = new AnalyseIndexThread(queueAnalyse, chainsAnalyse);
		}
		return r;
	}

	// 获取爬虫线程
	private static WebcrawlerThread[] getWebcrawlerThread(int size, SearchEngineQueue queueLinks,
			List<WebcrawlerChainExector> chainsWebcrawler, URLFilter filter) {
		WebcrawlerThread[] res = new WebcrawlerThread[size];
		for (int i = 0; i < res.length; i++) {

			res[i] = new WebcrawlerThread(queueLinks, chainsWebcrawler, filter);
		}
		return res;
	}

	/**
	 * 启动
	 */
	public static void start() {
		startThread();
		startMonitor();
	}

	/**
	 * 启动监控线程
	 */
	public static void startMonitor() {
		monitorThread.start();
		SelfLogger.log("monitor thread  threshold->" + WebcrawlerEnvManager.getThreshold());
	}

	/**
	 * 启动爬虫和分析线程
	 */
	public static void startThread() {
		for (WebcrawlerThread th : webcrawlerThreads) {
			th.start();
		}
		SelfLogger.log("Webcrawler thread size->"+webcrawlerThreads.length);
		for (AnalyseIndexThread th : analyseIndexThreads) {
			th.start();
		}
		SelfLogger.log("analyseIndex thread size->"+analyseIndexThreads.length);
		analysePersistenceThread.start();
	}

	// 获取分析调用链
	public static List<AnalyseChainExector> getChainsAnalyse(ACAutomaton acAllWords, TempIndexMap<Long, Long> map) {
		List<AnalyseChainExector> chains = new LinkedList<AnalyseChainExector>();
		chains.add(new AnalyseBuildTempIndexChainExector(acAllWords, map));
		return chains;
	}

	// 获取爬虫调用链
	public static List<WebcrawlerChainExector> getChainsWebcrawler(SearchEngineQueue queueLinks,
			SearchEngineQueue queueAnalyse, ConcurrentFileQueue queueUrlId) {
		List<WebcrawlerChainExector> chains = new LinkedList<WebcrawlerChainExector>();
		// 获取所有的url并重新放入队列
		chains.add(new WebcrawlerHrefGetExector(queueLinks));
		// 格式化HTML
		chains.add(new WebcrawlerFormatDocChainExector());
		// 放入分析队列
		chains.add(new WebcrawlerAddAnalyseQueueExector(queueAnalyse, queueUrlId));
		return chains;
	}

	// 获取过滤器
	public static URLFilter getFilter() {
		// 位图大小设置为Integer.MAX_VALUE 即0和正数都包含
		// 在这里我曾经纠结bitmap这样设置是否能够存放第Integer.MAX_VALUE个数据
		// 后面debug后发现 Integer.MAX_VALUE除以32是不能整除的(余数31),因为Integer.max = 2^31-1.
		// 因此刚好有一个位置的冗余，可以存放第Integer.MAX_VALUE个数据
		BitMap bitmap = new BitMapMemoryImpl(Integer.MAX_VALUE);
		BloomHash[] hashs = new BloomHash[5];
		// 固定5个哈希算法
		hashs[0] = new JavaHash();
		hashs[1] = new Md5Hash();
		hashs[2] = new SimpleHash(1);
		hashs[3] = new Md5Hash("salt");
		hashs[4] = new SimpleHash(2);
		BloomFilter filter = new BloomFilter(bitmap, hashs);
		return filter;
	}
}
