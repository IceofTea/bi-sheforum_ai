package com.henry.forum.admin.entity;

import java.util.List;

public class UserInfo {
   
		private Integer id;

	private String username;
	private String nickname;
	private String sex;
	private String password;

	private String head;

	private String realname;

	private String phone;

	private Integer status;
	
	private Integer experience = 0;
	private Integer level = 1;
	private Integer points = 0;
	private Integer threadCount = 0;
	private Integer commentCount = 0;
	private Integer loginCount = 0;
	private String lastLoginTime;
	private String registerTime;
	private Integer continuousLoginDays = 0;
	
	private List<UserUpload> userUploads;
	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", username=" + username + ", nickname=" + nickname + ", sex=" + sex
				+ ", password=" + password + ", head=" + head + ", realname=" + realname + ", phone=" + phone
				+ ", status=" + status + ", experience=" + experience + ", level=" + level + ", points=" + points + "]";
	}
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username == null ? null : username.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password == null ? null : password.trim();
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head == null ? null : head.trim();
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname == null ? null : realname.trim();
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone == null ? null : phone.trim();
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getExperience() {
		return experience;
	}

	public void setExperience(Integer experience) {
		this.experience = experience == null ? 0 : experience;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level == null ? 1 : level;
	}

	public Integer getPoints() {
		return points;
	}

	public void setPoints(Integer points) {
		this.points = points == null ? 0 : points;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount == null ? 0 : threadCount;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount == null ? 0 : commentCount;
	}

	public Integer getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount == null ? 0 : loginCount;
	}

	public String getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(String registerTime) {
		this.registerTime = registerTime;
	}

	public Integer getContinuousLoginDays() {
		return continuousLoginDays;
	}

	public void setContinuousLoginDays(Integer continuousLoginDays) {
		this.continuousLoginDays = continuousLoginDays == null ? 0 : continuousLoginDays;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public List<UserUpload> getUserUploads() {
		return userUploads;
	}
	public void setUserUploads(List<UserUpload> userUploads) {
		this.userUploads = userUploads;
	}
}