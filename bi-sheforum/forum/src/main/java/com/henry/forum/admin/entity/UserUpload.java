package com.henry.forum.admin.entity;

public class UserUpload {
	
	private Integer id;
	private Integer userInfoId;
	private String upload;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getUserInfoId() {
		return userInfoId;
	}
	public void setUserInfoId(Integer userInfoId) {
		this.userInfoId = userInfoId;
	}
	public String getUpload() {
		return upload;
	}
	public void setUpload(String upload) {
		this.upload = upload;
	}
	@Override
	public String toString() {
		return "UserUpload [id=" + id + ", userInfoId=" + userInfoId + ", upload=" + upload + "]";
	}
	
}
