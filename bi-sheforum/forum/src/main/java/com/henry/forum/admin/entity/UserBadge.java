package com.henry.forum.admin.entity;

public class UserBadge {
    private Integer id;
    private Integer userInfoId;
    private Integer badgeTypeId;
    private String earnedAt;
    private Integer isShow;
    private BadgeType badgeType;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserInfoId() { return userInfoId; }
    public void setUserInfoId(Integer userInfoId) { this.userInfoId = userInfoId; }
    public Integer getBadgeTypeId() { return badgeTypeId; }
    public void setBadgeTypeId(Integer badgeTypeId) { this.badgeTypeId = badgeTypeId; }
    public String getEarnedAt() { return earnedAt; }
    public void setEarnedAt(String earnedAt) { this.earnedAt = earnedAt; }
    public Integer getIsShow() { return isShow; }
    public void setIsShow(Integer isShow) { this.isShow = isShow; }
    public BadgeType getBadgeType() { return badgeType; }
    public void setBadgeType(BadgeType badgeType) { this.badgeType = badgeType; }
}