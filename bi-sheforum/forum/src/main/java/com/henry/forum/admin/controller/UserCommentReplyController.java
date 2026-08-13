package com.henry.forum.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserCommentReply;
import com.henry.forum.admin.service.UserCommentReplyService;

@RestController
@CrossOrigin(origins = { "*" })
@RequestMapping("api/reply")
public class UserCommentReplyController {
	@Autowired
	private UserCommentReplyService userCommentReplyService;

	@GetMapping
	public APIResult list(Integer user_comment_id) {
		
		return APIResult.ok(userCommentReplyService.selectAll(user_comment_id));
	}
	@PostMapping
	public APIResult add(@RequestBody UserCommentReply userCommentReply) {
		return APIResult.ok(userCommentReplyService.insert(userCommentReply));
	}
	@DeleteMapping
	public APIResult remove(Integer id) {
		return APIResult.ok(userCommentReplyService.remove(id));
	}
}
