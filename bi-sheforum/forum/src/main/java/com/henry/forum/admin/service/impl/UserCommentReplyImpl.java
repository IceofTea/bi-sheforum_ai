package com.henry.forum.admin.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserCommentReply;
import com.henry.forum.admin.mapper.UserCommentReplyMapper;
import com.henry.forum.admin.service.UserCommentReplyService;
@Service
public class UserCommentReplyImpl implements UserCommentReplyService {

	@Autowired
	private UserCommentReplyMapper commentReplyMapper;
	@Override
	public List<UserCommentReply> selectAll(Integer id) {
		
		return commentReplyMapper.selectReplyAll(id);
	}

	@Override
	public UserCommentReply selectById(Integer id) {
		
		return null;
	}

	@Override
	public UserCommentReply remove(Integer id) {
		
		UserCommentReply commentReply=commentReplyMapper.selectByPrimaryKey(id);
		commentReplyMapper.deleteByPrimaryKey(id);
		return commentReply;
	}

	@Override
	public UserCommentReply insert(UserCommentReply commentReply) {
		
		commentReplyMapper.insert(commentReply);
		return commentReplyMapper.selectByPrimaryKey(commentReply.getId());
	}

}
