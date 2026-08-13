package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserDailyExp {
    private Integer id;
    private Integer userInfoId;
    @JsonFormat(pattern = "yyyy年MM月dd号", timezone = "GMT+8")
    private Date expDate;
    private Integer expGot;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserInfoId() { return userInfoId; }
    public void setUserInfoId(Integer userInfoId) { this.userInfoId = userInfoId; }
    public Date getExpDate() { return expDate; }
    public void setExpDate(Date expDate) { this.expDate = expDate; }
    public Integer getExpGot() { return expGot; }
    public void setExpGot(Integer expGot) { this.expGot = expGot; }
}