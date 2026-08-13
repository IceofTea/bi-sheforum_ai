package com.henry.forum.admin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.Writer;
import com.henry.forum.admin.service.WriterService;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("api/writer")
public class WriterController {
	@Autowired
	private WriterService writerService;
	@GetMapping
	public APIResult list() {
		return APIResult.ok(writerService.selectAll());
	}
	@GetMapping("/one")
	public APIResult one(Integer id) {
		return APIResult.ok(writerService.selectById(id));
	}
	@PostMapping
	public APIResult add(@RequestBody Writer writer) {
		return APIResult.ok(writerService.insert(writer));
	}
	@PostMapping("/update")
	public APIResult update(@RequestBody Writer writer) {
		return APIResult.ok(writerService.edit(writer));
	}
}
