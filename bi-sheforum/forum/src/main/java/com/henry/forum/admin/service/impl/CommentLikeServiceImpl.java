package com.henry.forum.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.CommentLike;
import com.henry.forum.admin.mapper.CommentLikeMapper;
import com.henry.forum.admin.service.CommentLikeService;

@Service
public class CommentLikeServiceImpl implements CommentLikeService {
    @Autowired
    private CommentLikeMapper likeMapper;

    @Override
    public CommentLike like(Integer commentId, Integer userId) {
        if (hasLiked(commentId, userId)) return null;
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserInfoId(userId);
        likeMapper.insert(like);
        return like;
    }

    @Override
    public CommentLike unlike(Integer commentId, Integer userId) {
        likeMapper.delete(commentId, userId);
        return null;
    }

    @Override
    public int getLikeCount(Integer commentId) {
        return likeMapper.countByCommentId(commentId);
    }

    @Override
    public boolean hasLiked(Integer commentId, Integer userId) {
        return likeMapper.findByCommentAndUser(commentId, userId) != null;
    }
}