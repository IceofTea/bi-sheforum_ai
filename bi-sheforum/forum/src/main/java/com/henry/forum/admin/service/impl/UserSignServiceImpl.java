package com.henry.forum.admin.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserSign;
import com.henry.forum.admin.mapper.UserSignMapper;
import com.henry.forum.admin.service.UserInfoService;
import com.henry.forum.admin.service.UserSignService;

@Service
public class UserSignServiceImpl implements UserSignService {
    @Autowired
    private UserSignMapper signMapper;
    @Autowired
    private UserInfoService userInfoService;

    @Override
    public UserSign sign(Integer userId) {
        UserSign todaySign = signMapper.getTodaySign(userId);
        if (todaySign != null) {
            return null;
        }

        UserSign latestSign = signMapper.getLatestByUserId(userId);
        int continuousDays = 1;
        int points = 5;

        if (latestSign != null && latestSign.getSignDate() != null) {
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTime(latestSign.getSignDate());
            cal2.setTime(new Date());
            
            cal1.add(Calendar.DAY_OF_YEAR, 1);
            if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                continuousDays = latestSign.getContinuousDays() + 1;
            }
        }

        if (continuousDays >= 7) points = 15;
        else if (continuousDays >= 3) points = 10;
        else points = 5;

        UserSign sign = new UserSign();
        sign.setUserInfoId(userId);
        sign.setSignDate(new Date());
        sign.setPoints(points);
        sign.setContinuousDays(continuousDays);
        signMapper.insert(sign);

        userInfoService.updateContinuousLoginDays(userId, continuousDays);
        userInfoService.addExperience(userId, points);
        userInfoService.addPoints(userId, points);

        return sign;
    }

    @Override
    public List<UserSign> getSignHistory(Integer userId) {
        return signMapper.getAllByUserId(userId);
    }

    @Override
    public boolean checkTodaySign(Integer userId) {
        return signMapper.getTodaySign(userId) != null;
    }
}