package com.lahuan.common.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * AC自动机  专为匹配HTML标签而做的改版 仅考虑单标签匹配 即模糊匹配标签内的属性 更多情况并未考虑 
 * 仅匹配<a*>,</a>,<br>,<br/>这类写法写法,  <a*> 可以匹配<a>和<a href='xxxx' >等几种情况
 * *代表模糊匹配(0~n个任意字符，采用最小匹配原则)
 * 因为考虑的是标签匹配 不考虑*开头和*结尾的情况 并且不考虑一些非法标签 如<html*>abc </html*>
 * 
 * @author lfy
 */

public class ACAutomatonForTag {

	public static void main(String[] args) {
		// 模式串
		List<String> strs = Arrays.asList("<img*>","<a*>","</a>","<h1*>","</h1*>","<br>","<br/>","<select*/select>");
		//List<String> strs = Arrays.asList("<a*>");
		// 主串
		String text = "<img src='/web/1'  /><a href = 'abc'> xxx???</a><br><>>22,<br/><h1>aaa</h1><select><option>1</option></select>";// 主串
		// 初始化
		ACAutomatonForTag acc = new ACAutomatonForTag(strs);
		// 进行匹配
		LinkedList<ACAutomatonResult> r = acc.match(text);
		Iterator<ACAutomatonResult> iterator = r.descendingIterator();
		while(iterator.hasNext()){
			ACAutomatonResult res = iterator.next();
			System.out.println(res);
		}
	}
	/**
	 * 匹配
	 */
	public LinkedList<ACAutomatonResult> match(String s) {
		return match(s.toCharArray());

	}
	public ACAutomatonForTag(List<String> strs) {
		for (String s : strs) {
			insert(s);
		}
		build();
	}

	public ACAutomatonForTag(String[] strs) {
		for (String s : strs) {
			insert(s);
		}
		build();
	}

	public ACAutomatonForTag(String s) {
		insert(s);
		build();
	}

	Node root = new Node('/');// 根节点
	// 插入

	private void insert(String s) {
		if(s.startsWith("*") || s.endsWith("*")){
			throw new RuntimeException("不支持*开头和*结尾的模式串");
		}
		insert(s.toCharArray());
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
	 * 匹配
	 */
	public LinkedList<ACAutomatonResult> match(char[] text) { // text 是主串
		LinkedList<ACAutomatonResult> res = new LinkedList<ACAutomatonResult>();
		int len = text.length;
		// 带头指针
		// 即当前匹配字符的前一个字符
		Node p = root;
		for (int i = 0; i < len; i++) {
			int lenExt = 0;
			char idx = text[i];// 当前值的索引
			// 当前带头指针的后一个字符和当前值不相等
			// 就去找失败指针重新进行匹配 直到找不到 或者root为止
			Node next = p.getChild(idx);
			while (p != root && next == null) {//
				Node all = p.getChild('*');
				if( all !=null && i < len-1 ){
					//* 匹配0个的时候
					Node tmpNext = all.getChild(text[i]);
					if(tmpNext !=null){
						lenExt=-1;
						next = tmpNext;
						break;
					}
					//*匹配1个~n个的时候
					int tmpI= i;
					int tmpLenExt=lenExt;
					tmpNext = all.getChild(text[++tmpI]);
					while( tmpI < len-1 && tmpNext ==null ){
						tmpNext = all.getChild(text[++tmpI]);
						tmpLenExt++;
						while(tmpNext !=null &&!tmpNext.isEnd ){
							//末尾为多字符的情况
							tmpNext= tmpNext.getChild(text[++tmpI]);
							tmpLenExt++;
						}
					}
					if(tmpNext!=null){
						//匹配成功
						next = tmpNext;
						lenExt=tmpLenExt;
						i=tmpI;
						break;
					}
					
					
					
				}
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
				try {
					if (temp.isEnd == true) {
						// 偏移量和长度
						int pos = i - temp.len + 1-lenExt;
						int l = p.len+lenExt;
						// 匹配的字符串
						String str = new String(text, pos, l);
						ACAutomatonResult r = new ACAutomatonResult(pos, l, str);
						res.add(r);
					}
				} catch (Exception e) {
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

	// trie树的节点
	private class Node {

		private char v;// 值

		// private Node[] n = new Node[26];// 下一个节点也可以理解成子节点
		// 为匹配中文 使用hashmap记录子节点
		private HashMap<Character, Node> children = new HashMap<Character, Node>();

		private Node f;// 失败指针

		private boolean isEnd = false;// 是否最后一个词缀

		private int len = -1;// 从根到这里的长度

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
