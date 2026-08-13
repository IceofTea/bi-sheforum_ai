package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserSign {
    private Integer id;
    private Integer userInfoId;
    @JsonFormat(pattern = "yyyy年MM月dd号", timezone = "GMT+8")
    private Date signDate;
    private Integer points;
    private Integer continuousDays;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserInfoId() { return userInfoId; }
    public void setUserInfoId(Integer userInfoId) { this.userInfoId = userInfoId; }
    public Date getSignDate() { return signDate; }
    public void setSignDate(Date signDate) { this.signDate = signDate; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public Integer getContinuousDays() { return continuousDays; }
    public void setContinuousDays(Integer continuousDays) { this.continuousDays = continuousDays; }
}