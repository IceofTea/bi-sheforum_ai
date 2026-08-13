package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.Writer;
import com.henry.forum.admin.mapper.WriterMapper;
import com.henry.forum.admin.service.WriterService;
@Service
public class WriterServiceImpl implements WriterService {

	@Autowired
	private WriterMapper writerMapper;
	@Override
	public List selectAll() {
		
		return writerMapper.selectAll();
	}

	@Override
	public Writer selectById(Integer id) {
		
		return writerMapper.selectByPrimaryKey(id);
	}

	@Override
	public Writer remove(Integer id) {
		
		return null;
	}

	@Override
	public Writer edit(Writer writer) {
		
		writerMapper.updateByPrimaryKey(writer);
		return writerMapper.selectByPrimaryKey(writer.getId());
	}
	@Override
	public Writer insert(Writer writer) {
		
		writerMapper.insert(writer);
		return writerMapper.selectByPrimaryKey(writer.getId());
	}
	@Override
	public PageInfo getForPage(Integer pageIndex, Integer pageSize) {
		
		return null;
	}

}
