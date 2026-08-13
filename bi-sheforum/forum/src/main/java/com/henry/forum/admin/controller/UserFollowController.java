package com.henry.forum.admin.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserFollow;
import com.henry.forum.admin.service.UserFollowService;

@RestController
@RequestMapping("/api/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    @PostMapping
    public APIResult follow(@RequestParam Integer userId, @RequestParam Integer followUserId) {
        int result = userFollowService.follow(userId, followUserId);
        APIResult apiResult = new APIResult();
        if (result > 0) {
            apiResult.setStatus(200);
            apiResult.setMsg("关注成功");
        } else {
            apiResult.setStatus(400);
            apiResult.setMsg("关注失败或已关注");
        }
        return apiResult;
    }

    @DeleteMapping
    public APIResult unfollow(@RequestParam Integer userId, @RequestParam Integer followUserId) {
        int result = userFollowService.unfollow(userId, followUserId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setMsg(result > 0 ? "取消关注成功" : "取消关注失败");
        return apiResult;
    }

    @GetMapping("/following/{userId}")
    public APIResult getFollowingCount(@PathVariable Integer userId) {
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(userFollowService.getFollowingCount(userId));
        return apiResult;
    }

    @GetMapping("/follower/{userId}")
    public APIResult getFollowerCount(@PathVariable Integer userId) {
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(userFollowService.getFollowerCount(userId));
        return apiResult;
    }

    @GetMapping("/followinglist/{userId}")
    public APIResult getFollowingList(@PathVariable Integer userId) {
        List<UserFollow> list = userFollowService.getFollowingList(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(list);
        return apiResult;
    }

    @GetMapping("/followerlist/{userId}")
    public APIResult getFollowerList(@PathVariable Integer userId) {
        List<UserFollow> list = userFollowService.getFollowerList(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(list);
        return apiResult;
    }

    @GetMapping("/check")
    public APIResult isFollowing(@RequestParam Integer userId, @RequestParam Integer followUserId) {
        boolean result = userFollowService.isFollowing(userId, followUserId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(result);
        return apiResult;
    }
}