package com.henry.forum.admin.controller;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.ThreadsSort;
import com.henry.forum.admin.service.UserSortService;

@RestController
@RequestMapping("/api/usersort")
@CrossOrigin(origins = {"*"})
public class UserSortController {

    @Autowired
    private UserSortService userSortService;

    @GetMapping("/follow")
    public APIResult follow(@RequestParam Integer userId, @RequestParam Integer sortId) {
        userSortService.followSort(userId, sortId);
        return APIResult.ok("关注成功");
    }

    @GetMapping("/unfollow")
    public APIResult unfollow(@RequestParam Integer userId, @RequestParam Integer sortId) {
        userSortService.unfollowSort(userId, sortId);
        return APIResult.ok("取消关注成功");
    }

    @GetMapping("/followed")
    public APIResult getFollowed(@RequestParam Integer userId) {
        List<ThreadsSort> list = userSortService.getFollowedSorts(userId);
        return APIResult.ok(list);
    }

    @GetMapping("/isFollowed")
    public APIResult isFollowed(@RequestParam Integer userId, @RequestParam Integer sortId) {
        boolean followed = userSortService.isFollowed(userId, sortId);
        return APIResult.ok(followed);
    }

    @GetMapping("/visit")
    public APIResult recordVisit(@RequestParam Integer userId, @RequestParam Integer sortId, @RequestParam Integer visitType) {
        userSortService.recordVisit(userId, sortId, visitType);
        return APIResult.ok("记录成功");
    }

    @GetMapping("/mostVisited")
    public APIResult getMostVisited(@RequestParam Integer userId, @RequestParam(defaultValue = "5") Integer limit) {
        List<ThreadsSort> list = userSortService.getMostVisitedSorts(userId, limit);
        for (ThreadsSort sort : list) {
            sort.setThreadCount(userSortService.getThreadCount(sort.getId()));
        }
        return APIResult.ok(list);
    }

    @GetMapping("/created")
    public APIResult getCreated(@RequestParam Integer userId) {
        List<ThreadsSort> list = userSortService.getCreatedSorts(userId);
        for (ThreadsSort sort : list) {
            sort.setThreadCount(userSortService.getThreadCount(sort.getId()));
        }
        return APIResult.ok(list);
    }

    @GetMapping("/all")
    public APIResult getAll(@RequestParam(required = false) Integer userId) {
        List<ThreadsSort> list = userSortService.getAllSorts();
        if (userId != null) {
            Map<Integer, Map<String, Object>> userData = new HashMap<>();
            for (ThreadsSort sort : list) {
                Map<String, Object> data = new HashMap<>();
                data.put("followed", userSortService.isFollowed(userId, sort.getId()));
                data.put("created", userSortService.isCreated(userId, sort.getId()));
                data.put("threadCount", userSortService.getThreadCount(sort.getId()));
                userData.put(sort.getId(), data);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("sorts", list);
            result.put("userData", userData);
            return APIResult.ok(result);
        }
        return APIResult.ok(list);
    }

    @PostMapping("/create")
    public APIResult create(@RequestParam Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String introduction,
            @RequestParam(required = false) Integer parent) {
        ThreadsSort sort = new ThreadsSort();
        sort.setName(name);
        sort.setIntroduction(introduction);
        sort.setParent(parent);
        userSortService.createSort(sort, userId);
        return APIResult.ok(sort);
    }

    @PostMapping("/update")
    public APIResult update(@RequestBody ThreadsSort sort) {
        userSortService.updateSort(sort);
        return APIResult.ok(sort);
    }

    @DeleteMapping("/delete")
    public APIResult delete(@RequestParam Integer sortId, @RequestParam Integer userId) {
        userSortService.deleteSort(sortId, userId);
        return APIResult.ok("删除成功");
    }
}