package com.henry.forum.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.service.CommentLikeService;

@RestController
@RequestMapping("/api/commentlike")
@CrossOrigin(origins = {"*"})
public class CommentLikeController {
    @Autowired
    private CommentLikeService likeService;

    @PostMapping
    public APIResult toggle(@RequestBody Map<String, Integer> params) {
        Integer commentId = params.get("commentId");
        Integer userId = params.get("userId");
        if (commentId == null || userId == null) {
            return APIResult.notFound("参数错误");
        }
        
        boolean hasLiked = likeService.hasLiked(commentId, userId);
        if (hasLiked) {
            likeService.unlike(commentId, userId);
        } else {
            likeService.like(commentId, userId);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", !hasLiked);
        result.put("count", likeService.getLikeCount(commentId));
        return APIResult.ok(result);
    }

    @GetMapping("/count")
    public APIResult count(Integer commentId) {
        return APIResult.ok(likeService.getLikeCount(commentId));
    }

    @GetMapping("/status")
    public APIResult status(@RequestParam Integer commentId, @RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("liked", likeService.hasLiked(commentId, userId));
        result.put("count", likeService.getLikeCount(commentId));
        return APIResult.ok(result);
    }
}