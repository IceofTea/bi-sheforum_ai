package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserCollect;

public interface UserCollectService {
	List selectAll();
	
	UserCollect selectById(Integer id);
	
	UserCollect remove(Integer id);
	
	UserCollect edit(UserCollect root);

	PageInfo getForPage(Integer pageIndex, Integer pageSize);

	List<UserCollect> selectByUserId(Integer id);

	UserCollect insert(UserCollect userCollect);
}
