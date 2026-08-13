package com.henry.forum.admin.service;

import java.util.List;

import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.entity.UserUpload;

public interface UserUploadService {

	List selectAll();

	UserUpload insert(UserUpload userUpload);
	UserUpload selectById(Integer id);
	
	UserUpload remove(Integer id);
	
	UserUpload edit(UserInfo root);

	List<UserUpload> selectByUserId(Integer id);
}
