package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserCollect;
import com.henry.forum.admin.mapper.UserCollectMapper;
import com.henry.forum.admin.service.UserCollectService;
@Service
public class UserCollectServiceImpl implements UserCollectService {

	@Autowired
	private UserCollectMapper userCollectMapper;
	
	@Override
	public List selectAll() {
		
		return userCollectMapper.selectAll();
	}

	@Override
	public UserCollect selectById(Integer id) {
		
		return null;
	}
	@Override
	public List<UserCollect> selectByUserId(Integer id) {
		
		return userCollectMapper.selectByUserKey(id);
	}


	@Override
	public UserCollect remove(Integer id) {
		
		UserCollect userCollect=userCollectMapper.selectByPrimaryKey(id);
		userCollectMapper.deleteByPrimaryKey(id);
		return userCollect;
	}

	@Override
	public UserCollect edit(UserCollect root) {
		
		return null;
	}

	@Override
	public PageInfo getForPage(Integer pageIndex, Integer pageSize) {
		
		return null;
	}

	@Override
	public UserCollect insert(UserCollect userCollect) {
		
		userCollectMapper.insert(userCollect);
		return userCollectMapper.selectByPrimaryKey(userCollect.getId());
	}

}
