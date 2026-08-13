package com.henry.forum.admin.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserRead;
import com.henry.forum.admin.service.UserReadService;

@RestController
@RequestMapping("/api/userread")
@CrossOrigin(origins = {"*"})
public class UserReadController {
	@Autowired
	private UserReadService userReadService;
	
	@GetMapping
	public APIResult list() {
		return APIResult.ok(userReadService.selectAll());
	}
	@GetMapping("/one")
	public APIResult one(Integer id) {
		return APIResult.ok(userReadService.selectByUserId(id));
	}
	
	@GetMapping("/unique")
	public APIResult uniqueCount(Integer id) {
		return APIResult.ok(userReadService.countUniqueByUserId(id));
	}
	
	@GetMapping("/count")
	public APIResult countByMonth() {
		List<UserRead> allReads = userReadService.selectAll();
		List<Map<String, Object>> result = new ArrayList<>();
		int[] readCounts = new int[12];
		int[] collectCounts = new int[12];
		
		for (UserRead read : allReads) {
			if (read.getTime() != null) {
				int month = read.getTime().getMonth() + 1;
				if (month >= 1 && month <= 12) {
					readCounts[month - 1]++;
				}
			}
		}
		
		for (int i = 0; i < 12; i++) {
			Map<String, Object> item = new HashMap<>();
			item.put("month", i + 1);
			item.put("readCount", readCounts[i]);
			item.put("collectCount", collectCounts[i]);
			result.add(item);
		}
		
		return APIResult.ok(result);
	}
	
	@PostMapping
	public APIResult add(@RequestBody UserRead userRead) {
		return APIResult.ok(userReadService.insert(userRead));
	}
	
}
