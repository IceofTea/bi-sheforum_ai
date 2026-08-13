package com.henry.forum.admin.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.BadgeType;
import com.henry.forum.admin.entity.UserBadge;
import com.henry.forum.admin.service.BadgeService;

@RestController
@RequestMapping("/api/badge")
public class BadgeController {

    @Autowired
    private BadgeService badgeService;

    @GetMapping("/user/{userId}")
    public APIResult getUserBadges(@PathVariable Integer userId) {
        List<UserBadge> badges = badgeService.getUserBadges(userId);
        APIResult result = new APIResult();
        result.setStatus(200);
        result.setMsg("获取成功");
        result.setData(badges);
        return result;
    }

    @GetMapping("/all")
    public APIResult getAllBadgeTypes() {
        List<BadgeType> badges = badgeService.getAllBadgeTypes();
        APIResult result = new APIResult();
        result.setStatus(200);
        result.setMsg("获取成功");
        result.setData(badges);
        return result;
    }

    @PostMapping("/check/{userId}")
    public APIResult checkAndAwardBadges(@PathVariable Integer userId) {
        badgeService.checkAndAwardBadges(userId);
        List<UserBadge> badges = badgeService.getUserBadges(userId);
        APIResult result = new APIResult();
        result.setStatus(200);
        result.setMsg("徽章检查并发放成功");
        result.setData(badges);
        return result;
    }

    @PostMapping("/award")
    public APIResult awardBadge(@RequestParam Integer userId, @RequestParam Integer badgeTypeId) {
        badgeService.awardBadge(userId, badgeTypeId);
        APIResult result = new APIResult();
        result.setStatus(200);
        result.setMsg("徽章发放成功");
        result.setData(null);
        return result;
    }
}