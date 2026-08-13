package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.entity.UserInfo;

public interface UserInfoService {

	List selectAll();
	
	UserInfo selectById(Integer id);
	
	UserInfo remove(Integer id);
	
	UserInfo edit(UserInfo root);

	PageInfo getForPage(Integer pageIndex, Integer pageSize);

	List getRankings(String type);

	UserInfo login(UserInfo root) throws Exception;

	UserInfo register(UserInfo userInfo) throws Exception;
	
	void addExperience(Integer userId, Integer exp);
	
	void addPoints(Integer userId, Integer points);
	
	UserInfo updateLoginStats(Integer userId);
	
	int calculateLevel(int experience);

	UserInfo selectByUsername(String username);
	
	void incrementThreadCount(Integer userId);
	
	void incrementCommentCount(Integer userId);
	
	void updateContinuousLoginDays(Integer userId, Integer days);
}
