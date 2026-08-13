package com.henry.forum.admin.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserComment {
   
	private Integer id;
    private Integer userInfoId;
    private Integer threadInfoId;
    private String comment;
    @JsonFormat(pattern = "yyyy年MM月dd号 hh:mm:ss",timezone = "GMT+8")
    private Date time;
    private UserInfo userInfo;
    private ThreadInfo threadInfo;
    private List<UserCommentReply> replyContent;
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
	public String toString() {
		return "UserComment [id=" + id + ", userInfoId=" + userInfoId + ", threadInfoId=" + threadInfoId + ", comment="
				+ comment + ", time=" + time + ", userInfo=" + userInfo + ", threadInfo=" + threadInfo + ", replyContent="
				+ replyContent + "]";
	}

	public Integer getUserInfoId() {
        return userInfoId;
    }

    public void setUserInfoId(Integer userInfoId) {
        this.userInfoId = userInfoId;
    }

    public Integer getThreadInfoId() {
        return threadInfoId;
    }

    public void setThreadInfoId(Integer threadInfoId) {
        this.threadInfoId = threadInfoId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment == null ? null : comment.trim();
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public ThreadInfo getThreadInfo() {
		return threadInfo;
	}

	public void setThreadInfo(ThreadInfo threadInfo) {
		this.threadInfo = threadInfo;
	}

	public List<UserCommentReply> getReplyContent() {
		return replyContent;
	}

	public void setReplyContent(List<UserCommentReply> replyContent) {
		this.replyContent = replyContent;
	}
}