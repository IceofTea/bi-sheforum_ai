package com.henry.forum.admin.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class UserRead {
   
	private Integer id;
	@JsonFormat(pattern = "yyyy年MM月dd号 hh:mm:ss",timezone = "GMT+8")
	private Date time;

	private Integer threadInfoId;

	private Integer userInfoId;

	private ThreadInfo threadInfo;

	private UserInfo userInfo;

	
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


	public ThreadInfo getThreadInfo() {
		return threadInfo;
	}

	public void setThreadInfo(ThreadInfo threadInfo) {
		this.threadInfo = threadInfo;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public Integer getThreadInfoId() {
		return threadInfoId;
	}

	public void setThreadInfoId(Integer threadInfoId) {
		this.threadInfoId = threadInfoId;
	}

	public Integer getUserInfoId() {
		return userInfoId;
	}

	public void setUserInfoId(Integer userInfoId) {
		this.userInfoId = userInfoId;
	}

}