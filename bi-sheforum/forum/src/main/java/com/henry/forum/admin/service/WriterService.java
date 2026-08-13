package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.Writer;

public interface WriterService {
	List selectAll();
	
	Writer selectById(Integer id);
	
	Writer remove(Integer id);
	
	Writer edit(Writer writer);

	PageInfo getForPage(Integer pageIndex, Integer pageSize);

	Writer insert(Writer writer);
}
