package com.henry.forum.admin.entity;

public class ThreadInfo {


	private Integer id;
	private Integer writerId;
	private Writer writerContant;
	private String writer;
	private String name;
	private String introduction;
	private String picture;
	private String text;
	private String status;
	private Integer isupload;
	private Integer threadsSortId;
	private ThreadsSort threadsSort;
	private Integer views;
	private Integer commentCount;
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getWriter() {
		return writer;
	}

	public void setWriter(String writer) {
		this.writer = writer == null ? null : writer.trim();
	}

	public ThreadsSort getThreadsSort() {
		return threadsSort;
	}

	public void setThreadsSort(ThreadsSort threadsSort) {
		this.threadsSort = threadsSort;
	}
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public Integer getIsupload() {
		return isupload;
	}

	public void setIsupload(Integer isupload) {
		this.isupload = isupload;
	}

	public Writer getWriterContant() {
		return writerContant;
	}

	public void setWriterContant(Writer writerContant) {
		this.writerContant = writerContant;
	}

	public Integer getWriterId() {
		return writerId;
	}

	public void setWriterId(Integer writerId) {
		this.writerId = writerId;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getThreadsSortId() {
		return threadsSortId;
	}

	public void setThreadsSortId(Integer threadsSortId) {
		this.threadsSortId = threadsSortId;
	}

	public Integer getViews() {
		return views;
	}

	public void setViews(Integer views) {
		this.views = views;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}
}