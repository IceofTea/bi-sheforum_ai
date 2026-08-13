package com.henry.forum.admin.service;

import java.util.List;

public interface ThreadsSortService {
	List selectAll();
	
	com.henry.forum.admin.entity.ThreadsSort selectById(Integer id);
	
	com.henry.forum.admin.entity.ThreadsSort remove(Integer id);
	
	com.henry.forum.admin.entity.ThreadsSort edit(com.henry.forum.admin.entity.ThreadsSort root);

	com.henry.forum.admin.entity.ThreadsSort insert(com.henry.forum.admin.entity.ThreadsSort root);

}
