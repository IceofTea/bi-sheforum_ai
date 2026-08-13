package com.henry.forum.admin.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.henry.forum.admin.mapper.UserSortMapper;
import com.henry.forum.admin.entity.ThreadsSort;
import com.henry.forum.admin.service.UserSortService;

@Service
public class UserSortServiceImpl implements UserSortService {

    @Autowired
    private UserSortMapper userSortMapper;

    @Override
    public void followSort(Integer userId, Integer sortId) {
        if (userSortMapper.isFollowed(userId, sortId) == 0) {
            userSortMapper.followSort(userId, sortId);
        }
    }

    @Override
    public void unfollowSort(Integer userId, Integer sortId) {
        userSortMapper.unfollowSort(userId, sortId);
    }

    @Override
    public List<ThreadsSort> getFollowedSorts(Integer userId) {
        return userSortMapper.selectFollowedSorts(userId);
    }

    @Override
    public boolean isFollowed(Integer userId, Integer sortId) {
        return userSortMapper.isFollowed(userId, sortId) > 0;
    }

    @Override
    public void recordVisit(Integer userId, Integer sortId, Integer visitType) {
        int count = visitType == 1 ? 3 : 1;
        userSortMapper.addVisit(userId, sortId, visitType, count);
        
        userSortMapper.selectChildSortIds(sortId).forEach(childId -> {
            userSortMapper.addVisit(userId, childId, visitType, count);
        });
    }

    @Override
    public List<ThreadsSort> getMostVisitedSorts(Integer userId, Integer limit) {
        return userSortMapper.selectMostVisitedSorts(userId, limit);
    }

    @Override
    public List<ThreadsSort> getCreatedSorts(Integer userId) {
        return userSortMapper.selectCreatedSorts(userId);
    }

    @Override
    public boolean isCreated(Integer userId, Integer sortId) {
        return userSortMapper.isCreated(userId, sortId) > 0;
    }

    @Override
    public List<ThreadsSort> getAllSorts() {
        return userSortMapper.selectAllSorts();
    }

    @Override
    public void createSort(ThreadsSort sort, Integer userId) {
        userSortMapper.insertSort(sort);
        userSortMapper.createSort(userId, sort.getId());
    }

    @Override
    public void updateSort(ThreadsSort sort) {
        userSortMapper.updateSort(sort);
    }

    @Override
    public void deleteSort(Integer sortId, Integer userId) {
        userSortMapper.deleteCreatedSort(userId, sortId);
        userSortMapper.deleteSort(sortId);
    }

    @Override
    public int getThreadCount(Integer sortId) {
        return userSortMapper.countThreadsBySort(sortId);
    }
}