package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserMessage {
    private Integer id;
    private Integer userId;
    private Integer fromUserId;
    private String type;
    private Integer relatedId;
    private String content;
    private Integer isRead;
    @JsonFormat(pattern = "yyyy年MM月dd号 HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getFromUserId() { return fromUserId; }
    public void setFromUserId(Integer fromUserId) { this.fromUserId = fromUserId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}