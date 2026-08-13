package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserCommentReply {
  
	@Override
	public String toString() {
		return "UserCommentReply [id=" + id + ", userInfo=" + userInfo + ", userCommentId=" + userCommentId
				+ ", comment=" + comment + ", time=" + time + "]";
	}

	private Integer id;
    private UserInfo userInfo;
    private Integer userInfoId;
    private Integer userCommentId;
    private String comment;
    @JsonFormat(pattern = "yyyy年MM月dd号 hh:mm:ss",timezone = "GMT+8")
    private Date time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }


	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Integer getUserCommentId() {
		return userCommentId;
	}

	public void setUserCommentId(Integer userCommentId) {
		this.userCommentId = userCommentId;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Integer getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(Integer userInfoId) {
		this.userInfoId = userInfoId;
	}
}