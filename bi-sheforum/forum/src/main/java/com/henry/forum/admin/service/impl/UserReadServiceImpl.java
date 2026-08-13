package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.entity.UserRead;
import com.henry.forum.admin.mapper.UserReadMapper;
import com.henry.forum.admin.service.UserReadService;
@Service

public class UserReadServiceImpl implements UserReadService {

	@Autowired
	private UserReadMapper userReadMapper;
	@Override
	public UserRead insert(UserRead userRead) {
		
		userReadMapper.insert(userRead);
		return userReadMapper.selectByPrimaryKey(userRead.getId());
	}

	@Override
	public List selectAll() {
		
		return userReadMapper.selectAll();
	}

	@Override
	public UserRead selectById(Integer id) {
		
		return null;
	}
	@Override
	public List<UserRead> selectByUserId(Integer id) {
		
		return userReadMapper.selectByUserKey(id);
	}

	@Override
	public UserRead remove(Integer id) {
		
		return null;
	}

	@Override
	public UserRead edit(UserInfo root) {
		
		return null;
	}

	@Override
	public PageInfo getForPage(Integer pageIndex, Integer pageSize) {
		
		return null;
	}

	@Override
	public int countByThreadId(Integer threadInfoId) {
		return userReadMapper.countByThreadId(threadInfoId);
	}

	@Override
	public int countUniqueByUserId(Integer userInfoId) {
		return userReadMapper.countUniqueByUserId(userInfoId);
	}

}
