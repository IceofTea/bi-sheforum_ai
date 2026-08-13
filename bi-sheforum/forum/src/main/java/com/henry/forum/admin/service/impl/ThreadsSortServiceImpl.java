package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.mapper.ThreadsSortMapper;
import com.henry.forum.admin.service.ThreadsSortService;
@Service
public class ThreadsSortServiceImpl implements ThreadsSortService {

	@Autowired
	private ThreadsSortMapper threadsSortMapper;
	@Override
	public List selectAll() {
		return threadsSortMapper.selectAll();
	}

	@Override
	public com.henry.forum.admin.entity.ThreadsSort selectById(Integer id) {
		return threadsSortMapper.selectByPrimaryKey(id);
	}

	@Override
	public com.henry.forum.admin.entity.ThreadsSort remove(Integer id) {
		return null;
	}

	@Override
	public com.henry.forum.admin.entity.ThreadsSort edit(com.henry.forum.admin.entity.ThreadsSort root) {
		threadsSortMapper.updateByPrimaryKey(root);
		return threadsSortMapper.selectByPrimaryKey(root.getId());
	}

	@Override
	public com.henry.forum.admin.entity.ThreadsSort insert(com.henry.forum.admin.entity.ThreadsSort root) {
		threadsSortMapper.insert(root);
		return threadsSortMapper.selectByPrimaryKey(root.getId());
	}

}
