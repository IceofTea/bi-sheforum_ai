package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;

public interface ThreadInfoService {

	
	com.henry.forum.admin.entity.ThreadInfo selectById(Integer id);
	
	com.henry.forum.admin.entity.ThreadInfo selectByName(String name);
	
	List selectAll();
	
	com.henry.forum.admin.entity.ThreadInfo remove(Integer id);
	
	com.henry.forum.admin.entity.ThreadInfo edit(com.henry.forum.admin.entity.ThreadInfo threadInfo);

	PageInfo getForPage(Integer pageNum, Integer pageSize);

	com.henry.forum.admin.entity.ThreadInfo insert(com.henry.forum.admin.entity.ThreadInfo threadInfo);

	List<com.henry.forum.admin.entity.ThreadInfo> selectBySortId(Integer id);

	// 新增分页查询功能
	PageInfo getByWriterId(Integer writerId, Integer page, Integer limit);

	// 新增按分类ID查询
	List<com.henry.forum.admin.entity.ThreadInfo> selectAllBySort(Integer sortId);

	List<com.henry.forum.admin.entity.ThreadInfo> selectRecommended();

	int countByWriterId(Integer writerId);
	
	List<com.henry.forum.admin.entity.ThreadInfo> selectAllByWriter(Integer writerId);
}
