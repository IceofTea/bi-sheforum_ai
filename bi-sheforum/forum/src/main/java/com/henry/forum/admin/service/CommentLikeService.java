package com.henry.forum.admin.service;

import com.henry.forum.admin.entity.CommentLike;

public interface CommentLikeService {
    CommentLike like(Integer commentId, Integer userId);
    CommentLike unlike(Integer commentId, Integer userId);
    int getLikeCount(Integer commentId);
    boolean hasLiked(Integer commentId, Integer userId);
}