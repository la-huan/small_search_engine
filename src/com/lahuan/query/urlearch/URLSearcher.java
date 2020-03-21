package com.lahuan.query.urlearch;

import java.util.List;

import com.lahuan.query.entity.ResultMapping;
import com.lahuan.query.entity.UrlInfo;

/**
 * 根据词ID获取URL
 * 
 * @author la-huan
 *
 */
public interface URLSearcher {
	/**
	 * 根据词ID获取URL的ID
	 */
	public List<ResultMapping> query(List<Long> ids);

	/**
	 * 根据词ID获取URL的ID
	 */
	public List<ResultMapping> query(Long[] ids);

	/**
	 * 根据词匹配信息获取URL的信息
	 */
	public List<UrlInfo> query(ResultMapping m);
}
