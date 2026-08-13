package com.henry.forum.admin.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserSign;
import com.henry.forum.admin.service.UserSignService;

@RestController
@RequestMapping("/api/sign")
@CrossOrigin(origins = {"*"})
public class UserSignController {
    @Autowired
    private UserSignService signService;

    @PostMapping
    public APIResult sign(@RequestBody Map<String, Integer> params) {
        Integer userId = params.get("userId");
        if (userId == null) {
            return APIResult.notFound("请先登录");
        }

        UserSign sign = signService.sign(userId);
        if (sign == null) {
            return APIResult.notFound("今日已签到");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("points", sign.getPoints());
        result.put("continuousDays", sign.getContinuousDays());
        result.put("msg", "签到成功！连续签到" + sign.getContinuousDays() + "天");
        return APIResult.ok(result);
    }

    @GetMapping("/status")
    public APIResult checkSign(Integer userId) {
        if (userId == null) {
            return APIResult.ok(false);
        }
        return APIResult.ok(signService.checkTodaySign(userId));
    }

    @GetMapping("/history")
    public APIResult getHistory(Integer userId) {
        if (userId == null) {
            return APIResult.notFound("请先登录");
        }
        return APIResult.ok(signService.getSignHistory(userId));
    }
}