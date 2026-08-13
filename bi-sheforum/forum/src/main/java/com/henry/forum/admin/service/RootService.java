package com.henry.forum.admin.service;

import com.henry.forum.admin.entity.Root;

public interface RootService {
	Root login(Root root) throws Exception;
	
	Root register(Root root);
	
	Root selectById(Integer id);
	
	Root remove(Integer id);
	
	Root edit(Root root);

	Root selectByName(String name);
}
