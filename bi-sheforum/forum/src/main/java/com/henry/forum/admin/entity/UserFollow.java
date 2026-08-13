package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserFollow {
    private Integer id;
    private Integer userId;
    private Integer followUserId;
    @JsonFormat(pattern = "yyyy年MM月dd号 HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getFollowUserId() { return followUserId; }
    public void setFollowUserId(Integer followUserId) { this.followUserId = followUserId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}