package com.henry.forum.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.ThreadsSort;
import com.henry.forum.admin.service.ThreadsSortService;

@RestController
@RequestMapping("/api/sort")
@CrossOrigin(origins = {"*"})
public class ThreadSortController {
	@Autowired
	private ThreadsSortService threadsSortService;
	
	@GetMapping
	public APIResult list() {
		return APIResult.ok(threadsSortService.selectAll());
	}
	@GetMapping("/one")
	public APIResult one(Integer id) {
		return APIResult.ok(threadsSortService.selectById(id));
	}
	
	@PostMapping
	public APIResult insert(ThreadsSort threadsSort) {
		return APIResult.ok(threadsSortService.insert(threadsSort));
	}
	@PostMapping("/update")
	public APIResult update(@RequestBody ThreadsSort threadsSort) {
//		System.out.println(threadsSort);
		return APIResult.ok(threadsSortService.edit(threadsSort));
	}
}
