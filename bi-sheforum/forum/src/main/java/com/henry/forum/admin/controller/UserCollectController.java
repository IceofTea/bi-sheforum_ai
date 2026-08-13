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
import com.henry.forum.admin.entity.UserCollect;
import com.henry.forum.admin.service.UserCollectService;

@RestController
@RequestMapping("/api/usercollect")
@CrossOrigin(origins = {"*"})
public class UserCollectController {
	@Autowired
	private UserCollectService userCollectService;

	@GetMapping
	public APIResult list() {
		return APIResult.ok(userCollectService.selectAll());
	}
	@GetMapping("/one")
	public APIResult one(Integer id) {
		return APIResult.ok(userCollectService.selectByUserId(id));
	}

	@GetMapping("/byuser")
	public APIResult byUser(Integer userId) {
		return APIResult.ok(userCollectService.selectByUserId(userId));
	}

	@PostMapping
	public APIResult add(@RequestBody UserCollect userCollect) {
		return APIResult.ok(userCollectService.insert(userCollect));
	}
	@DeleteMapping
	public APIResult remove(Integer id) {
		return APIResult.ok(userCollectService.remove(id));
	}
}