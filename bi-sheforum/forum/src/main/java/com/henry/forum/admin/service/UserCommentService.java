package com.henry.forum.admin.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.UserCollect;
import com.henry.forum.admin.entity.UserComment;

public interface UserCommentService {
	List selectAll();

	UserComment selectById(Integer id);

	UserComment remove(Integer id);

	UserComment edit(UserCollect root);

	PageInfo getForPage(Integer pageIndex, Integer pageSize);

	List selectByThreadId(Integer id);

	List selectByUserId(Integer id);

	UserComment insert(UserComment userComment);

	int countByThreadId(Integer threadInfoId);
}
