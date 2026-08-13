package com.henry.forum.admin.entity;

public class ThreadsSort {

	private Integer id;

	private String name;

	private String picture;

	private Integer parent;

	private ThreadsSort parentcontent;

	private Integer status;
	
	private String introduction;
	
	private Integer threadCount;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name == null ? null : name.trim();
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture == null ? null : picture.trim();
	}

	public Integer getParent() {
		return parent;
	}

	public void setParent(Integer parent) {
		this.parent = parent;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public ThreadsSort getParentcontent() {
		return parentcontent;
	}

	public void setParentcontent(ThreadsSort parentcontent) {
		this.parentcontent = parentcontent;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public Integer getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(Integer threadCount) {
		this.threadCount = threadCount;
	}
}