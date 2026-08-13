package com.henry.forum.admin.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.henry.forum.admin.entity.BadgeType;
import com.henry.forum.admin.entity.UserBadge;
import com.henry.forum.admin.entity.UserInfo;
import com.henry.forum.admin.mapper.BadgeTypeMapper;
import com.henry.forum.admin.mapper.UserBadgeMapper;
import com.henry.forum.admin.mapper.UserInfoMapper;
import com.henry.forum.admin.service.BadgeService;

@Service
public class BadgeServiceImpl implements BadgeService {

    @Autowired
    private UserBadgeMapper userBadgeMapper;

    @Autowired
    private BadgeTypeMapper badgeTypeMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserBadge> getUserBadges(Integer userId) {
        List<UserBadge> userBadges = userBadgeMapper.selectByUserId(userId);
        for (UserBadge ub : userBadges) {
            if (ub.getBadgeTypeId() != null) {
                BadgeType bt = userBadgeMapper.selectBadgeTypeById(ub.getBadgeTypeId());
                ub.setBadgeType(bt);
            }
        }
        return userBadges;
    }

    @Override
    public List<BadgeType> getAllBadgeTypes() {
        return badgeTypeMapper.selectAll();
    }

    @Override
    @Transactional
    public void checkAndAwardBadges(Integer userId) {
        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) return;

        if (user.getThreadCount() != null && user.getThreadCount() > 0) {
            awardBadgesForCondition(userId, "thread_count", user.getThreadCount());
        }
        if (user.getCommentCount() != null && user.getCommentCount() > 0) {
            awardBadgesForCondition(userId, "comment_count", user.getCommentCount());
        }
        if (user.getLoginCount() != null && user.getLoginCount() > 0) {
            awardBadgesForCondition(userId, "login_count", user.getLoginCount());
        }
        if (user.getLevel() != null && user.getLevel() > 1) {
            awardBadgesForCondition(userId, "level", user.getLevel());
        }
        if (user.getContinuousLoginDays() != null && user.getContinuousLoginDays() > 0) {
            awardBadgesForCondition(userId, "sign_days", user.getContinuousLoginDays());
        }
    }

    private void awardBadgesForCondition(Integer userId, String conditionType, Integer currentValue) {
        List<BadgeType> badgesToAward = userBadgeMapper.selectBadgesToAward(conditionType, currentValue);
        for (BadgeType badge : badgesToAward) {
            if (userBadgeMapper.countByUserAndBadge(userId, badge.getId()) == 0) {
                awardBadge(userId, badge.getId());
            }
        }
    }

    @Override
    @Transactional
    public void awardBadge(Integer userId, Integer badgeTypeId) {
        BadgeType badgeType = badgeTypeMapper.selectById(badgeTypeId);
        if (badgeType == null) return;

        if (userBadgeMapper.countByUserAndBadge(userId, badgeTypeId) > 0) return;

        UserBadge userBadge = new UserBadge();
        userBadge.setUserInfoId(userId);
        userBadge.setBadgeTypeId(badgeTypeId);
        userBadge.setIsShow(1);
        userBadgeMapper.insert(userBadge);

        UserInfo user = userInfoMapper.selectByPrimaryKey(userId);
        if (user == null) return;

        if (badgeType.getPointsReward() != null && badgeType.getPointsReward() > 0) {
            user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + badgeType.getPointsReward());
        }
        if (badgeType.getExpReward() != null && badgeType.getExpReward() > 0) {
            user.setExperience((user.getExperience() == null ? 0 : user.getExperience()) + badgeType.getExpReward());
            user.setLevel(calculateLevel(user.getExperience()));
        }
        userInfoMapper.updateByPrimaryKey(user);
    }

    private int calculateLevel(int experience) {
        if (experience < 100) return 1;
        if (experience < 300) return 2;
        if (experience < 600) return 3;
        if (experience < 1000) return 4;
        if (experience < 1500) return 5;
        if (experience < 2100) return 6;
        if (experience < 2800) return 7;
        if (experience < 3600) return 8;
        if (experience < 4500) return 9;
        if (experience < 5500) return 10;
        if (experience < 6600) return 11;
        if (experience < 7800) return 12;
        if (experience < 9100) return 13;
        if (experience < 10500) return 14;
        if (experience < 12000) return 15;
        if (experience < 13600) return 16;
        if (experience < 15300) return 17;
        if (experience < 17100) return 18;
        if (experience < 19000) return 19;
        return 20;
    }
}