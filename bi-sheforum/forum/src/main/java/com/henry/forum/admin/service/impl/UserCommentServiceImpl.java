package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserCollect;
import com.henry.forum.admin.entity.UserComment;
import com.henry.forum.admin.mapper.UserCommentMapper;
import com.henry.forum.admin.service.UserCommentService;
@Service
public class UserCommentServiceImpl implements UserCommentService {

	@Autowired
	private UserCommentMapper commentMapper;
	
	@Override
	public UserComment insert(UserComment userComment) {
		commentMapper.insert(userComment);
		return commentMapper.selectByPrimaryKey(userComment.getId());
	}

	@Override
	public List selectAll() {
		return commentMapper.selectAll();
	}

	@Override
	public UserComment selectById(Integer id) {
		return commentMapper.selectByPrimaryKey(id);
	}

	@Override
	public List selectByThreadId(Integer id) {
		return commentMapper.selectByThreadId(id);
	}

	@Override
	public UserComment remove(Integer id) {
		UserComment userComment = commentMapper.selectByPrimaryKey(id);
		commentMapper.deleteByPrimaryKey(id);
		return userComment;
	}

	@Override
	public UserComment edit(UserCollect root) {
		return null;
	}

	@Override
	public PageInfo getForPage(Integer pageIndex, Integer pageSize) {
		return null;
	}

	@Override
	public int countByThreadId(Integer threadInfoId) {
		return commentMapper.countByThreadId(threadInfoId);
	}

	@Override
	public List selectByUserId(Integer id) {
		return commentMapper.selectByUserId(id);
	}

}
