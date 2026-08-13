package com.henry.forum.admin.entity;

import java.util.List;

public class Writer {

	private Integer id;

    private String name;

    private String birthday;

    private String sex;

    private String address;

    private String introduction;
    
    private String head;

    private List<ThreadInfo> threadInfos; 
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex == null ? null : sex.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction == null ? null : introduction.trim();
    }

	public List<ThreadInfo> getThreadInfos() {
		return threadInfos;
	}

	public void setThreadInfos(List<ThreadInfo> threadInfos) {
		this.threadInfos = threadInfos;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}
}