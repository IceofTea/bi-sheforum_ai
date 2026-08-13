package com.henry.forum.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserComment;
import com.henry.forum.admin.service.UserCommentService;
import com.henry.forum.admin.service.UserDailyExpService;
import com.henry.forum.admin.service.UserInfoService;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(origins = {"*"})
public class UserCommentController {

	@Autowired
	private UserCommentService commentService;
	@Autowired
	private UserDailyExpService dailyExpService;
	@Autowired
	private UserInfoService userInfoService;

	private static final int COMMENT_EXP = 3;
	private static final int DAILY_EXP_LIMIT = 50;
	
	@GetMapping
	public APIResult list() {
		return APIResult.ok(commentService.selectAll());
	}
	@GetMapping("/bythread")
	public APIResult getbythreadid(Integer id) {
		return APIResult.ok(commentService.selectByThreadId(id));
	}
	@GetMapping("/byuser")
	public APIResult getbyuserid(Integer id) {
		return APIResult.ok(commentService.selectByUserId(id));
	}
	@PostMapping
	public APIResult add(@RequestBody UserComment userComment) {
		UserComment result = commentService.insert(userComment);
		Map<String, Object> expInfo = null;
		
		if (result != null && userComment.getUserInfoId() != null) {
			userInfoService.incrementCommentCount(userComment.getUserInfoId());
			expInfo = new HashMap<>();
			
			if (dailyExpService.canGainExp(userComment.getUserInfoId(), COMMENT_EXP)) {
				dailyExpService.addExp(userComment.getUserInfoId(), COMMENT_EXP);
				userInfoService.addExperience(userComment.getUserInfoId(), COMMENT_EXP);
				userInfoService.addPoints(userComment.getUserInfoId(), COMMENT_EXP);
				expInfo.put("gained", true);
				expInfo.put("exp", COMMENT_EXP);
			} else {
				int todayExp = dailyExpService.getTodayExp(userComment.getUserInfoId());
				expInfo.put("gained", false);
				expInfo.put("reason", "今日经验已达上限(" + DAILY_EXP_LIMIT + ")，请明天再来");
				expInfo.put("todayExp", todayExp);
			}
			expInfo.put("limit", DAILY_EXP_LIMIT);
			result.setTime(userComment.getTime());
		}
		
		Map<String, Object> response = new HashMap<>();
		response.put("comment", result);
		response.put("expInfo", expInfo);
		return APIResult.ok(response);
	}
	@DeleteMapping
	public APIResult remove(Integer id) {
		return APIResult.ok(commentService.remove(id));
	}
}
