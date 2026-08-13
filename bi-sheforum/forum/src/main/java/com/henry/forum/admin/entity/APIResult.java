package com.henry.forum.admin.entity;

public class APIResult {
	private Integer status;
	private String msg;
	private Object data;
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	/**
	 * get/delete成功
	 * @param object
	 * @return
	 */
	public static APIResult ok(Object object) {
		APIResult apiResult=new APIResult();
		apiResult.setStatus(200);
		apiResult.setData(object);
		return apiResult;
	}
	/**
	 * post/put成功
	 * @param object
	 * @return
	 */
	public static APIResult created(Object object) {
		APIResult apiResult=new APIResult();
		apiResult.setStatus(201);
		apiResult.setData(object);
		return apiResult;
	}
	/**
	 * 404notfound
	 * @param msg
	 * @return
	 */
	public static APIResult notFound(String msg) {
		APIResult apiResult=new APIResult();
		apiResult.setStatus(404);
		apiResult.setMsg(msg);
		return apiResult;
	}
	/**
	 * 用户未认证，未登录
	 * @param msg
	 * @return
	 */
	public static APIResult unauthorized(String msg) {
		APIResult apiResult=new APIResult();
		apiResult.setStatus(401);
		apiResult.setMsg(msg);
		return apiResult;
	}
}

