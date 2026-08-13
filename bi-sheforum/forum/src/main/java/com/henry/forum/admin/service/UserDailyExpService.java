package com.henry.forum.admin.service;

public interface UserDailyExpService {
    boolean canGainExp(Integer userId, int expAmount);
    void addExp(Integer userId, int expAmount);
    int getTodayExp(Integer userId);
}