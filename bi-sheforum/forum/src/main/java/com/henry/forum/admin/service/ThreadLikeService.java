package com.henry.forum.admin.service;

import com.henry.forum.admin.entity.ThreadLike;

public interface ThreadLikeService {
    ThreadLike like(Integer threadId, Integer userId);
    ThreadLike unlike(Integer threadId, Integer userId);
    int getLikeCount(Integer threadId);
    boolean hasLiked(Integer threadId, Integer userId);
}