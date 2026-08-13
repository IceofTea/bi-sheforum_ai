package com.henry.forum.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.ThreadLike;
import com.henry.forum.admin.mapper.ThreadLikeMapper;
import com.henry.forum.admin.service.ThreadLikeService;

@Service
public class ThreadLikeServiceImpl implements ThreadLikeService {
    @Autowired
    private ThreadLikeMapper likeMapper;

    @Override
    public ThreadLike like(Integer threadId, Integer userId) {
        if (hasLiked(threadId, userId)) return null;
        ThreadLike like = new ThreadLike();
        like.setThreadInfoId(threadId);
        like.setUserInfoId(userId);
        likeMapper.insert(like);
        return like;
    }

    @Override
    public ThreadLike unlike(Integer threadId, Integer userId) {
        likeMapper.delete(threadId, userId);
        return null;
    }

    @Override
    public int getLikeCount(Integer threadId) {
        return likeMapper.countByThreadId(threadId);
    }

    @Override
    public boolean hasLiked(Integer threadId, Integer userId) {
        return likeMapper.findByThreadAndUser(threadId, userId) != null;
    }
}