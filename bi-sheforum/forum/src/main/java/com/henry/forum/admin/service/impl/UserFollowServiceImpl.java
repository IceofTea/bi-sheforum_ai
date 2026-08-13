package com.henry.forum.admin.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserFollow;
import com.henry.forum.admin.mapper.UserFollowMapper;
import com.henry.forum.admin.service.UserFollowService;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    public int follow(Integer userId, Integer followUserId) {
        if (userId == null || followUserId == null || userId.equals(followUserId)) {
            return 0;
        }
        if (userFollowMapper.find(userId, followUserId) != null) {
            return 0;
        }
        UserFollow follow = new UserFollow();
        follow.setUserId(userId);
        follow.setFollowUserId(followUserId);
        return userFollowMapper.insert(follow);
    }

    @Override
    public int unfollow(Integer userId, Integer followUserId) {
        if (userId == null || followUserId == null) {
            return 0;
        }
        return userFollowMapper.delete(userId, followUserId);
    }

    @Override
    public int getFollowingCount(Integer userId) {
        return userId == null ? 0 : userFollowMapper.countFollowings(userId);
    }

    @Override
    public int getFollowerCount(Integer userId) {
        return userId == null ? 0 : userFollowMapper.countFollowers(userId);
    }

    @Override
    public List<UserFollow> getFollowingList(Integer userId) {
        return userId == null ? null : userFollowMapper.getFollowingList(userId);
    }

    @Override
    public List<UserFollow> getFollowerList(Integer userId) {
        return userId == null ? null : userFollowMapper.getFollowerList(userId);
    }

    @Override
    public boolean isFollowing(Integer userId, Integer followUserId) {
        if (userId == null || followUserId == null) {
            return false;
        }
        return userFollowMapper.find(userId, followUserId) != null;
    }
}