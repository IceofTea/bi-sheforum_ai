package com.henry.forum.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.service.ThreadLikeService;

@RestController
@RequestMapping("/api/threadlike")
@CrossOrigin(origins = {"*"})
public class ThreadLikeController {
    @Autowired
    private ThreadLikeService likeService;

    @PostMapping
    public APIResult toggle(@RequestBody Map<String, Integer> params) {
        Integer threadId = params.get("threadId");
        Integer userId = params.get("userId");
        if (threadId == null || userId == null) {
            return APIResult.notFound("参数错误");
        }
        
        boolean hasLiked = likeService.hasLiked(threadId, userId);
        if (hasLiked) {
            likeService.unlike(threadId, userId);
        } else {
            likeService.like(threadId, userId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", !hasLiked);
        result.put("count", likeService.getLikeCount(threadId));
        return APIResult.ok(result);
    }

    @GetMapping("/count")
    public APIResult count(Integer threadId) {
        return APIResult.ok(likeService.getLikeCount(threadId));
    }

    @GetMapping("/status")
    public APIResult status(@RequestParam Integer threadId, @RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("liked", likeService.hasLiked(threadId, userId));
        result.put("count", likeService.getLikeCount(threadId));
        return APIResult.ok(result);
    }
}