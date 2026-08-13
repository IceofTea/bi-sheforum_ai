package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class ThreadLike {
    private Integer id;
    private Integer threadInfoId;
    private Integer userInfoId;
    @JsonFormat(pattern = "yyyy年MM月dd号 HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getThreadInfoId() { return threadInfoId; }
    public void setThreadInfoId(Integer threadInfoId) { this.threadInfoId = threadInfoId; }
    public Integer getUserInfoId() { return userInfoId; }
    public void setUserInfoId(Integer userInfoId) { this.userInfoId = userInfoId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}