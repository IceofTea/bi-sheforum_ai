package com.henry.forum.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserDailyExp;
import com.henry.forum.admin.mapper.UserDailyExpMapper;
import com.henry.forum.admin.service.UserDailyExpService;

@Service
public class UserDailyExpServiceImpl implements UserDailyExpService {
    @Autowired
    private UserDailyExpMapper dailyExpMapper;

    private static final int DAILY_EXP_LIMIT = 50;

    @Override
    public boolean canGainExp(Integer userId, int expAmount) {
        int todayExp = getTodayExp(userId);
        return todayExp + expAmount <= DAILY_EXP_LIMIT;
    }

    @Override
    public void addExp(Integer userId, int expAmount) {
        if (userId == null) return;
        UserDailyExp todayExp = dailyExpMapper.getTodayByUserId(userId);
        if (todayExp == null) {
            UserDailyExp newExp = new UserDailyExp();
            newExp.setUserInfoId(userId);
            newExp.setExpGot(0);
            dailyExpMapper.insert(newExp);
            todayExp = dailyExpMapper.getTodayByUserId(userId);
        }
        if (todayExp == null) return;
        
        int currentExp = todayExp.getExpGot() == null ? 0 : todayExp.getExpGot();
        int newExp = currentExp + expAmount;
        if (newExp > DAILY_EXP_LIMIT) {
            newExp = DAILY_EXP_LIMIT;
        }
        dailyExpMapper.updateTodayExp(userId, newExp);
    }

    @Override
    public int getTodayExp(Integer userId) {
        if (userId == null) return 0;
        UserDailyExp todayExp = dailyExpMapper.getTodayByUserId(userId);
        return todayExp == null || todayExp.getExpGot() == null ? 0 : todayExp.getExpGot();
    }
}