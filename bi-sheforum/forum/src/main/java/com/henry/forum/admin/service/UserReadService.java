package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.entity.UserRead;

public interface UserReadService {
	List selectAll();
	
	UserRead selectById(Integer id);
	
	UserRead remove(Integer id);
	
	UserRead edit(UserInfo root);

	PageInfo getForPage(Integer pageIndex, Integer pageSize);

	List<UserRead> selectByUserId(Integer id);

	UserRead insert(UserRead userRead);

	int countByThreadId(Integer threadInfoId);

	int countUniqueByUserId(Integer userInfoId);
}
