package com.henry.forum.admin.service;

import java.util.List;
import com.henry.forum.admin.entity.ThreadsSort;

public interface UserSortService {
    
    void followSort(Integer userId, Integer sortId);
    
    void unfollowSort(Integer userId, Integer sortId);
    
    List<ThreadsSort> getFollowedSorts(Integer userId);
    
    boolean isFollowed(Integer userId, Integer sortId);
    
    void recordVisit(Integer userId, Integer sortId, Integer visitType);
    
    List<ThreadsSort> getMostVisitedSorts(Integer userId, Integer limit);
    
    List<ThreadsSort> getCreatedSorts(Integer userId);
    
    boolean isCreated(Integer userId, Integer sortId);
    
    List<ThreadsSort> getAllSorts();
    
    void createSort(ThreadsSort sort, Integer userId);
    
    void updateSort(ThreadsSort sort);
    
    void deleteSort(Integer sortId, Integer userId);
    
    int getThreadCount(Integer sortId);
}