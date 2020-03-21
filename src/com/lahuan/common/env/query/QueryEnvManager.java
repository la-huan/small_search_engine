package com.lahuan.common.env.query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.lahuan.analyse.tempindex.TempIndexMap;
import com.lahuan.analyse.tempindex.TempIndexMemoryHashMap;
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
import com.lahuan.common.search.ACAutomatonResult;
import com.lahuan.common.util.SelfLogger;
import com.lahuan.query.entity.ResultMapping;
import com.lahuan.query.entity.UrlInfo;
import com.lahuan.query.urlearch.URLBinarySearcher;
import com.lahuan.query.urlearch.URLSearcher;
import com.lahuan.query.wordfetch.WordsDefaultSearch;
import com.lahuan.query.wordfetch.WordsSearcher;

/**
 * 查找环境
 * 
 * @author la-huan
 *
 */
public class QueryEnvManager {
	public static void main(String[] args) throws Exception {
		init();
		checkAll(false);
	}

	// url查找器
	private static URLSearcher urlSearcher;
	// 词查找器
	private static WordsSearcher wordsFetcher;

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
	// 持久化倒排索引结果和偏移量
	private static AnalysePersistenceThread analysePersistenceThread;
	// 倒排索引暂存的map
	private static TempIndexMap<Long, Long> tempIndexMap;

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
		QueryGlobelContext.put(GlobelConfig.QUEUE_LINKS_KEY, queueLinks);
		// 分析队列
		queueAnalyse = new SearchEngineFileQueue(GlobelConfig.getFilePath(), GlobelConfig.QUEUE_ANALYSE_NAME,
				GlobelConfig.QUEUE_FILE_PER_MAX);
		QueryGlobelContext.put(GlobelConfig.QUEUE_ANALYSE_KEY, queueAnalyse);
		// URL的ID队列
		queueUrlId = new ConcurrentFileQueue(GlobelConfig.getFilePath(), GlobelConfig.QUEUE_URL_ID_NAME);
		QueryGlobelContext.put(GlobelConfig.QUEUE_URL_ID_KEY, queueUrlId);

		//////////////////////////////////////////////////////////////

		// 根据词库创建ac自动机
		acAllWords = ACAutomaton.getACAutomatonByWordsPath(GlobelConfig.DEFAULT_WORDS_FILE_PATH);
		QueryGlobelContext.put(GlobelConfig.GLOBEL_WORDS_AUTOMATON, acAllWords);
		// 倒排索引暂存的map
		tempIndexMap = new TempIndexMemoryHashMap<Long, Long>();
		QueryGlobelContext.put(GlobelConfig.ANALYSE_TMP_IDX_MAP_KEY, tempIndexMap);

		/////////////////////////////////////////////////////////////////
		// 持久化索引
		persistenceidx = new PersistenceIndexFileImpl(GlobelConfig.getPersistenceIdxFilePath());
		QueryGlobelContext.put(GlobelConfig.PERSISTENCE_FILE_INDEX_KEY, persistenceidx);
		// 持久化偏移量
		persistenceOffset = new PersistenceOffsetFileImpl(GlobelConfig.getPersistenceOffsetFilePath());
		QueryGlobelContext.put(GlobelConfig.PERSISTENCE_FILE_OFFSET_KEY, persistenceOffset);
		// 持久化倒排索引结果和偏移量
		analysePersistenceThread = new AnalysePersistenceThread(tempIndexMap, persistenceidx, persistenceOffset);
		QueryGlobelContext.put(GlobelConfig.ANALYSE_PERSISTENCE_THREAD_KEY, analysePersistenceThread);

		// 在URL队列里放入种子信息 重复放入也没事 有布隆过滤器 也不会重复爬取

		// URL查找器
		urlSearcher = new URLBinarySearcher(persistenceidx, persistenceOffset, queueUrlId,
				(SearchEngineFileQueue) queueAnalyse);
		QueryGlobelContext.put(GlobelConfig.QUERY_URL_SEARCHER_KEY, urlSearcher);
		// 词库查找器
		wordsFetcher = new WordsDefaultSearch(acAllWords);
		QueryGlobelContext.put(GlobelConfig.QUERY_WORDS_SEARCHER_KEY, wordsFetcher);

	}

	public static void checkRange(int i, int j) throws Exception {
		int size = persistenceOffset.getSize();
		System.out.println("size:" + size);
		for (; i < size && i < j; i++) {
			long[] offset = persistenceOffset.readOffsetByIdx(i);
			// System.out.println("offset:"+Arrays.toString(offset));
			byte[] read = persistenceidx.read(offset[0], offset[1]);
			System.out.println(new ResultMapping(new String(read)).parse(acAllWords, queueUrlId,
					(SearchEngineFileQueue) queueAnalyse));

		}
	}

	/**
	 * 输出全部索引
	 * 
	 * @param persistenceidx
	 * @param persistenceOffset
	 * @param acAllWords
	 * @param queueUrlId
	 * @param queueAnalyse
	 * @throws Exception
	 */
	public static void checkAll(boolean flag) throws Exception {
		int size = persistenceOffset.getSize();
		System.out.println("size:" + size);
		for (int i = 0; i < size; i++) {
			long[] offset = persistenceOffset.readOffsetByIdx(i);
			// System.out.println("offset:"+Arrays.toString(offset));
			byte[] read = persistenceidx.read(offset[0], offset[1]);
			ResultMapping mapp = new ResultMapping(new String(read));
			System.out.println(i + "->" + Arrays.toString(offset) + "->" + mapp);
			//
			if (flag)
				System.out.println(mapp.parse(acAllWords, queueUrlId, (SearchEngineFileQueue) queueAnalyse));
		}
	}

	/**
	 * 查询 未去重
	 * 
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static List<UrlInfo> query(String text) throws Exception {
		List<UrlInfo> res = new LinkedList<UrlInfo>();
		List<ACAutomatonResult> wordsResult = wordsFetcher.query(text);
		List<Long> wordIds = new LinkedList<Long>();
	
		for (ACAutomatonResult r : wordsResult) {
			wordIds.add(r.getWordId());
			System.out.println("词ID:" + r.getWordId() + ",词:" + r.getStr());
		}
		System.out.println("===============");
		List<ResultMapping> query = urlSearcher.query(wordIds);
		for (ResultMapping m : query) {
			List<UrlInfo> info = urlSearcher.query(m);
			res.addAll(info);
		}
		SelfLogger.log("query:"+text+", match words size:"+wordsResult.size()+",result size:"+res.size());
		return res;
	}
	
	public static void queryAndPrint(String text) throws Exception{
		List<UrlInfo> res = query(text);
		for (UrlInfo u : res) {
			System.out.println("===");
			System.out.println("URL_ID:" + u.getUrlId());
			System.out.println("URL:" + u.getUrl());
			System.out.println("URL 标题:" + u.getTitle());
			System.out.println("URL keyword:" + u.getKeyword());
			System.out.println("URL description:" + u.getDescription());
			System.out.println("URL 内容摘要:" + u.getSummary());
		}
	};
}
