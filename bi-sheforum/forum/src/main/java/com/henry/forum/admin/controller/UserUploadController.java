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
import com.henry.forum.admin.entity.UserUpload;
import com.henry.forum.admin.service.UserUploadService;

@RestController
@RequestMapping("/api/useruploads")
@CrossOrigin(origins = {"*"})
public class UserUploadController {
	@Autowired
	private UserUploadService userUploadService;
	
	@GetMapping
	public APIResult getlist(Integer id){
		return APIResult.ok(userUploadService.selectByUserId(id));
	}
	
	@GetMapping("/one")
	public APIResult one(Integer id){
		return APIResult.ok(userUploadService.selectByUserId(id));
	}
	
	@PostMapping
	public APIResult insert(@RequestBody UserUpload userUpload){
		return APIResult.ok(userUploadService.insert(userUpload));
	}
	@DeleteMapping
	public APIResult remove(Integer id) {
		return APIResult.ok(userUploadService.remove(id));
	}
}
