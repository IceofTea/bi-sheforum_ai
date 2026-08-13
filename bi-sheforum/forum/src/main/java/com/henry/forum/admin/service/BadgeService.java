package com.henry.forum.admin.service;

import java.util.List;
import com.henry.forum.admin.entity.BadgeType;
import com.henry.forum.admin.entity.UserBadge;

public interface BadgeService {
    List<UserBadge> getUserBadges(Integer userId);
    List<BadgeType> getAllBadgeTypes();
    void checkAndAwardBadges(Integer userId);
    void awardBadge(Integer userId, Integer badgeTypeId);
}