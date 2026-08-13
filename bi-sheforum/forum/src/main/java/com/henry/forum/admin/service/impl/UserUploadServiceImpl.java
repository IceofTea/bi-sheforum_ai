package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.entity.UserUpload;
import com.henry.forum.admin.mapper.UserUploadMapper;
import com.henry.forum.admin.service.UserUploadService;

@Service
public class UserUploadServiceImpl implements UserUploadService {

	@Autowired
	private UserUploadMapper userUploadMapper;

	@Override
	public List selectAll() {
		
		return null;
	}

	@Override
	public UserUpload selectById(Integer id) {
		
		return userUploadMapper.selectByPrimaryKey(id);
	}

	@Override
	public UserUpload remove(Integer id) {
		
		UserUpload upload = userUploadMapper.selectByPrimaryKey(id);
		userUploadMapper.deleteByPrimaryKey(id);
		return upload;
	}

	@Override
	public UserUpload edit(UserInfo root) {
		
		return null;
	}

	@Override
	public List<UserUpload> selectByUserId(Integer id) {
		
		return userUploadMapper.selectByUserKey(id);
	}

	@Override
	public UserUpload insert(UserUpload userUpload) {
		
		userUploadMapper.insert(userUpload);
		return userUploadMapper.selectByPrimaryKey(userUpload.getId());
	}

}
