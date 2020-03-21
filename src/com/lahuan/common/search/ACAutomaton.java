package com.lahuan.common.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import com.lahuan.common.idgenerator.WordIdGenerator;
import com.lahuan.common.idgenerator.WordIdGeneratorMemoryImpl;

/**
 * AC自动机
 * 
 * @description
 * 
 * @author lfy
 */

public class ACAutomaton {
	//
	public static void main(String[] args) throws Exception {
		String path = "D:\\temp\\words";
		ACAutomaton acAutomaton = getACAutomatonByWordsPath(path);
		acAutomaton.match("啊实打实的阿萨德阿萨德执行打算去问恶趣味阿萨德多少");
	}
	public static ACAutomaton getACAutomatonByWordsPath(String path){
		File dir = new File(path);
		File[] files = dir.listFiles();
		// File类的比较接口 是通过比较路径进行的
		Arrays.sort(files);
		LinkedList<String> words = new LinkedList<String>();
		for (File file : files) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				while(true){
					String word = reader.readLine();
					if(word==null){
						break;
					}
					int indexOf = word.indexOf("	");
					if (indexOf == -1) {
						indexOf = word.indexOf(" ");
					}
					if (indexOf != -1)
						word = word.substring(0, indexOf);
					words.add(word);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//for (String s : words) {
			//System.out.println(s);
			//Thread.sleep(1);
		//}
		System.out.println("ACAutomaton init success,words dir: "+dir.getAbsolutePath()+",number of words :"+words.size());
		ACAutomaton ac = new ACAutomaton(words, new WordIdGeneratorMemoryImpl());
		return ac;
	}
	
	
	Node root = new Node('/');// 根节点
	WordIdGenerator idGenerator;// id生成器
	private ConcurrentHashMap<Long, String> wordsMapping = new ConcurrentHashMap<Long, String>();//词ID和具体词之间的映射
	public ACAutomaton(List<String> strs, WordIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		for (String s : strs) {
			if (idGenerator != null) {
				insert(s, idGenerator.getId());
			} else {
				insert(s);
			}
		}
		build();
	}

	public ACAutomaton(String[] strs, WordIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		for (String s : strs) {
			if (idGenerator != null) {
				insert(s, idGenerator.getId());
			} else {
				insert(s);
			}
		}
		build();
	}

	public ACAutomaton(String s, WordIdGenerator idGenerator) {
		this.idGenerator = idGenerator;
		if (idGenerator != null) {
			insert(s, idGenerator.getId());
		} else {
			insert(s);
		}
		build();
	}

	// 插入
	private void insert(String s) {
		insert(s.replaceAll("\\s*", "").toCharArray());
	}

	// 插入
	private void insert(String s, long id) {
		insert(s.replaceAll("\\s*", "").toCharArray(), id);
		wordsMapping.put(id, s);
	}
	/**
	 * 获取ID
	 * @param id
	 * @return
	 */
	public String getWordByid(long id){
		return wordsMapping.get(id);
	}
	/**
	 * 匹配
	 */
	public List<ACAutomatonResult> match(String s) {
		return match(s.toCharArray());
	}

	// 构建失效指针
	private void build() {
		// 从根节点开始生成
		Queue<Node> queue = new LinkedList<>();
		root.f = null;// root匹配 方便后面进行跳出
		Collection<Node> leve1 = root.children.values();// 第一级的树
		for (Node cn : leve1) {
			if (cn != null) {
				// 从第二级开始
				queue.add(cn);
				cn.f = root;// 第一级子节点失败全部指向root
			}
		}
		// 动态规划
		while (!queue.isEmpty()) {
			// 取出元素 父级节点
			Node p = queue.remove();
			// 子节点
			Collection<Node> values = p.children.values();
			for (Node c : values) {
				if (c == null)
					continue;
				queue.add(c);
				Node f = p.f;// 父节点的fail节点
				while (f != null) {
					Node node = f.getChild(c.v);
					if (node != null) {
						// 父节点的失败节点里查找子节点
						// 找到就可以跳出了
						c.f = node;
						break;
					}
					// 找不到 就找失败节点的失败节点 直到root
					// 如果root都没有就结束循环 跳出后赋值为root 下面的if判断
					f = f.f;

				}
				if (f == null) {
					// 找不到的时候 在上面的循环里 如果找到了失败节点就会跳出 f不会为空
					c.f = root;
				}
			}

		}

	}
	/**
	 * 判断字符串是否存在
	 * @param str
	 * @return
	 */
	public boolean exist(String str){
		return exist(str.toCharArray());
	}
	/**
	 * 判断字符串是否存在
	 * @param str
	 * @return
	 */
	public boolean exist(char[] c) {
		Node cur = root;
		for (int i = 0; i < c.length; i++) {
			cur = cur.getChild(c[i]);
		}
		return cur.isEnd;
	}
	/**
	 * 匹配
	 */
	private List<ACAutomatonResult> match(char[] text) { // text 是主串
		ArrayList<ACAutomatonResult> res = new ArrayList<ACAutomatonResult>();
		int len = text.length;
		// 带头指针
		// 即当前匹配字符的前一个字符
		Node p = root;
		for (int i = 0; i < len; i++) {
			char idx = text[i];// 当前值的索引
			// 当前带头指针的后一个字符和当前值不相等
			// 就去找失败指针重新进行匹配 直到找不到 或者root为止
			Node next = p.getChild(idx);
			while (p != root && next == null) {//
				p = p.f;
				next = p.getChild(idx);
			}
			// 匹配的话 p就前进一个节点
			// 如果不匹配 p就会变成null 在下面的循环里就会被重设为root
			p = next;
			// 找不到匹配值的时候 重设为root
			if (p == null) {
				p = root;
			}
			// 遍历节点和当前节点的失效指针
			// 判断是否是结束节点
			Node temp = p;
			while (temp != root) {
				if (temp.isEnd == true) {
					try {
						// 偏移量和长度
						int pos = i - temp.len + 1;
						int l = p.len;
						// 匹配的字符串
						String str = new String(text, pos, l);
						ACAutomatonResult r = new ACAutomatonResult(pos, l, str, temp.wordId);
						res.add(r);
					} catch (Exception e) {
					}
				}
				temp = temp.f;// 重设设失效指针，即相同后缀的指针 判断isEnd值
			}
		}
		return res;
	}

	/**
	 * 插入到trie树
	 * 
	 * @param text
	 */
	private void insert(char[] text) {
		Node p = root;
		for (int i = 0; i < text.length; ++i) {
			if (p.getChild(text[i]) == null) {
				Node newNode = new Node(text[i]);
				p.setChild(text[i], newNode);
			}
			p = p.getChild(text[i]);
			p.len = i + 1;// 深度
		}
		//
		p.isEnd = true;
		p.len = text.length;
	}

	/**
	 * 插入到trie树
	 * 
	 * @param text
	 */
	private void insert(char[] text, long wordId) {
		Node p = root;
		for (int i = 0; i < text.length; ++i) {
			if (p.getChild(text[i]) == null) {
				Node newNode = new Node(text[i]);
				p.setChild(text[i], newNode);
			}
			p = p.getChild(text[i]);
			p.len = i + 1;// 深度
		}
		//
		p.isEnd = true;
		p.len = text.length;
		p.wordId = wordId;
	}

	// trie树的节点
	private class Node {

		private char v;// 值

		// private Node[] n = new Node[26];// 下一个节点也可以理解成子节点
		// 为匹配中文 使用hashmap记录子节点
		private HashMap<Character, Node> children = new HashMap<Character, Node>();

		private Node f;// 失败指针

		private boolean isEnd = false;// 是否最后一个词缀

		private int len = -1;// 从根到这里的长度

		private long wordId = -1;

		/**
		 * 设置子节点
		 * 
		 * @param c
		 * @param node
		 */
		private void setChild(char c, Node node) {
			Character key = Character.valueOf(c);
			children.put(key, node);
		}

		/**
		 * 获取子节点
		 * 
		 * @param c
		 * @return
		 */
		private Node getChild(char c) {
			Character key = Character.valueOf(c);
			return children.get(key);
		}

		private Node(char v) {
			this.v = v;
		}

		@Override
		public String toString() {
			if (f != this)
				return "Node [v=" + v + ", f=" + f + "]";
			return "Node [v=" + v + ", f= root]";
		}

	}
}
