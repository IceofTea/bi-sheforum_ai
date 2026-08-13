package com.henry.forum.admin.service;

import java.util.List;

import com.henry.forum.admin.entity.UserCommentReply;

public interface UserCommentReplyService {
	List<UserCommentReply> selectAll(Integer user_comment_id);
	
	UserCommentReply selectById(Integer id);
	
	UserCommentReply remove(Integer id);

	UserCommentReply insert(UserCommentReply commentReply);
}
