package com.henry.forum.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserComment;
import com.henry.forum.admin.entity.UserRead;
import com.henry.forum.admin.service.UserCommentService;
import com.henry.forum.admin.service.UserReadService;

@RestController
@RequestMapping("/api/bot")
@CrossOrigin(origins = {"*"})
public class BotController {

	@Autowired
	private UserReadService userReadService;

	@GetMapping("/replied")
	public APIResult getRepliedPosts(Integer userId) {
		if (userId == null) {
			return APIResult.notFound("用户ID不能为空");
		}
		List<UserRead> records = userReadService.selectByUserId(userId);
		return APIResult.ok(records);
	}

	@PostMapping("/replied")
	public APIResult addRepliedPost(@RequestBody UserRead userRead) {
		if (userRead.getUserInfoId() == null || userRead.getThreadInfoId() == null) {
			return APIResult.notFound("参数不完整");
		}
		userReadService.insert(userRead);
		return APIResult.ok("记录成功");
	}
}