package com.henry.forum.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.Root;
import com.henry.forum.admin.mapper.RootMapper;
import com.henry.forum.admin.service.RootService;

@Service
public class RootServiceImpl implements RootService {

	@Autowired
	private RootMapper rootMapper;
	@Override
	public Root login(Root root) throws Exception {
		//1.账户密码格式是否正确
		if (root.getRootname()==null ||
			root.getRootname().length() < 3 ||
			root.getRootname().length() > 16){
			throw new Exception("账号格式不对");
		}
		if (root.getPassword( )==null ||
			root.getPassword().length() < 3 ||
			root.getPassword(). length() >16){
			throw new Exception("密码格式不对");
		}

		try {
			//2. 查询账户信息
			Root selecRoot=rootMapper.selectByName(root.getRootname());

			//3.判断账户是否存在
			if (selecRoot==null) {
				throw new Exception("账户不存在");
			}
			//4.判断密码是否正确
			if (!selecRoot.getPassword().equals(root.getPassword())) {
				throw new Exception("密码错误");
			}
			//5.判断账户是否可用
			if (selecRoot.getStatus()!=0) {
				throw new Exception("账户被禁用");
			}

			return selecRoot;
		} catch (Exception e) {
			// 添加更多调试信息
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public Root register(Root root) {
		
		rootMapper.insert(root);
		return rootMapper.selectByPrimaryKey(root.getId());
	}

	@Override
	public Root selectById(Integer id) {
		
		return rootMapper.selectByPrimaryKey(id);
	}
	@Override
	public Root selectByName(String name) {
		
		return rootMapper.selectByName(name);
	}
	@Override
	public Root remove(Integer id) {
		
		Root root=rootMapper.selectByPrimaryKey(id);
		rootMapper.deleteByPrimaryKey(id);
		return root;
	}

	@Override
	public Root edit(Root root) {
		
		rootMapper.updateByPrimaryKey(root);
		return rootMapper.selectByPrimaryKey(root.getId());
	}

}
