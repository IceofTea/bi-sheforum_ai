package com.henry.forum.admin.service;

import java.util.List;
import com.henry.forum.admin.entity.UserFollow;

public interface UserFollowService {
    int follow(Integer userId, Integer followUserId);
    int unfollow(Integer userId, Integer followUserId);
    int getFollowingCount(Integer userId);
    int getFollowerCount(Integer userId);
    List<UserFollow> getFollowingList(Integer userId);
    List<UserFollow> getFollowerList(Integer userId);
    boolean isFollowing(Integer userId, Integer followUserId);
}