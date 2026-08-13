package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.mapper.ThreadInfoMapper;
import com.henry.forum.admin.mapper.UserCommentMapper;
import com.henry.forum.admin.mapper.UserReadMapper;
import com.henry.forum.admin.service.ThreadInfoService;

@Service
public class ThreadInfoServiceImpl implements ThreadInfoService {

	@Autowired
	private ThreadInfoMapper threadInfoMapper;

	@Autowired
	private UserReadMapper userReadMapper;

	@Autowired
	private UserCommentMapper userCommentMapper;

	@Override
	public com.henry.forum.admin.entity.ThreadInfo selectById(Integer id) {
		com.henry.forum.admin.entity.ThreadInfo thread = threadInfoMapper.selectByPrimaryKey(id);
		if (thread != null) {
			thread.setViews(userReadMapper.countByThreadId(id));
			thread.setCommentCount(userCommentMapper.countByThreadId(id));
		}
		return thread;
	}

	@Override
	public com.henry.forum.admin.entity.ThreadInfo selectByName(String name) {
		return threadInfoMapper.selectByName(name);
	}

	@Override
	public List<com.henry.forum.admin.entity.ThreadInfo> selectBySortId(Integer id) {
		List<com.henry.forum.admin.entity.ThreadInfo> threads = threadInfoMapper.selectAllBySortId(id);
		for (com.henry.forum.admin.entity.ThreadInfo thread : threads) {
			thread.setViews(userReadMapper.countByThreadId(thread.getId()));
			thread.setCommentCount(userCommentMapper.countByThreadId(thread.getId()));
		}
		return threads;
	}

	@Override
	public List<com.henry.forum.admin.entity.ThreadInfo> selectAll() {
		List<com.henry.forum.admin.entity.ThreadInfo> threads = threadInfoMapper.selectAll();
		for (com.henry.forum.admin.entity.ThreadInfo thread : threads) {
			thread.setViews(userReadMapper.countByThreadId(thread.getId()));
			thread.setCommentCount(userCommentMapper.countByThreadId(thread.getId()));
		}
		return threads;
	}

	@Override
	public PageInfo getForPage(Integer page, Integer limit) {
		// 使用分页插件pagehelper进行物理分页
		PageHelper.startPage(page, limit);
		List threads = threadInfoMapper.selectAll();
		PageInfo pageInfo = new PageInfo(threads);
		return pageInfo;
	}

	@Override
	public com.henry.forum.admin.entity.ThreadInfo remove(Integer id) {
		com.henry.forum.admin.entity.ThreadInfo threadInfo = threadInfoMapper.selectByPrimaryKey(id);
		threadInfoMapper.deleteByPrimaryKey(id);
		return threadInfo;
	}

	@Override
	public com.henry.forum.admin.entity.ThreadInfo edit(com.henry.forum.admin.entity.ThreadInfo threadInfo) {
		threadInfoMapper.updateByPrimaryKey(threadInfo);
		return threadInfoMapper.selectByPrimaryKey(threadInfo.getId());
	}

	@Override
	public com.henry.forum.admin.entity.ThreadInfo insert(com.henry.forum.admin.entity.ThreadInfo threadInfo) {
		threadInfoMapper.insert(threadInfo);
		return threadInfoMapper.selectByPrimaryKey(threadInfo.getId());
	}

	@Override
	public PageInfo getByWriterId(Integer writerId, Integer page, Integer limit) {
		// 使用分页插件pagehelper进行物理分页
		PageHelper.startPage(page, limit);
		List<com.henry.forum.admin.entity.ThreadInfo> threads = threadInfoMapper.selectAllByWriter(writerId);
		return new PageInfo<>(threads);
	}

	// 新增按分类ID查询
	@Override
	public List<com.henry.forum.admin.entity.ThreadInfo> selectAllBySort(Integer sortId) {
		return threadInfoMapper.selectAllBySortId(sortId);
	}

	@Override
	public List<com.henry.forum.admin.entity.ThreadInfo> selectRecommended() {
		List<com.henry.forum.admin.entity.ThreadInfo> threads = threadInfoMapper.selectAll();
		for (com.henry.forum.admin.entity.ThreadInfo thread : threads) {
			thread.setViews(userReadMapper.countByThreadId(thread.getId()));
			thread.setCommentCount(userCommentMapper.countByThreadId(thread.getId()));
		}
		threads.sort((a, b) -> {
			int scoreA = (a.getViews() != null ? a.getViews() : 0) + (a.getCommentCount() != null ? a.getCommentCount() : 0) * 3;
			int scoreB = (b.getViews() != null ? b.getViews() : 0) + (b.getCommentCount() != null ? b.getCommentCount() : 0) * 3;
			if (scoreA != scoreB) return scoreB - scoreA;
			return b.getId() - a.getId();
		});
		return threads;
	}

	@Override
	public int countByWriterId(Integer writerId) {
		return threadInfoMapper.countByWriterId(writerId);
	}
	
	@Override
	public List<com.henry.forum.admin.entity.ThreadInfo> selectAllByWriter(Integer writerId) {
		return threadInfoMapper.selectAllByWriter(writerId);
	}
}
