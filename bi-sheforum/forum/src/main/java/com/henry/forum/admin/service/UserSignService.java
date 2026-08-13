package com.henry.forum.admin.service;

import java.util.List;

import com.henry.forum.admin.entity.UserSign;

public interface UserSignService {
    UserSign sign(Integer userId);
    List<UserSign> getSignHistory(Integer userId);
    boolean checkTodaySign(Integer userId);
}