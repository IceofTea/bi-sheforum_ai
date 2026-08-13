package com.henry.forum.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.pagehelper.PageInfo;
import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.ThreadInfo;
import com.henry.forum.admin.service.ThreadInfoService;
import com.henry.forum.admin.service.UserInfoService;

@RestController
@RequestMapping("/api/threads")
@CrossOrigin(origins = {"*"})
public class ThreadInfoController {
    @Autowired
    private ThreadInfoService threadInfoService;
    
    @Autowired
    private UserInfoService userInfoService;
    
    @GetMapping
    public APIResult list(Integer pageSize, Integer pageIndex) {
        return APIResult.ok(threadInfoService.getForPage(pageIndex, pageSize));
    }
    @GetMapping("/one")
    public APIResult one(Integer id) {
        return APIResult.ok(threadInfoService.selectById(id));
    }
    @GetMapping("/bysort")
    public APIResult bysort(Integer id) {
        return APIResult.ok(threadInfoService.selectBySortId(id));
    }
    @GetMapping("/all")
    public APIResult alllist() {
        return APIResult.ok(threadInfoService.selectAll());
    }
    @GetMapping("/recommend")
    public APIResult recommended() {
        return APIResult.ok(threadInfoService.selectRecommended());
    }
    
    @GetMapping("/count")
    public APIResult countByWriterId(Integer writerId) {
        return APIResult.ok(threadInfoService.countByWriterId(writerId));
    }
    
    @GetMapping("/byuser")
    public APIResult byuser(Integer writerId) {
        return APIResult.ok(threadInfoService.selectAllByWriter(writerId));
    }
    
    @PostMapping("/update")
    public APIResult update(ThreadInfo threadInfo) {
        return APIResult.ok(threadInfoService.edit(threadInfo));
    }
    @PostMapping
    public APIResult insert(
            String name,
            String introduction,
            String picture,
            Integer threadsSortId,
            String writer,
            Integer writerId) {
        ThreadInfo threadInfo = new ThreadInfo();
        threadInfo.setName(name);
        threadInfo.setIntroduction(introduction);
        threadInfo.setPicture(picture);
        threadInfo.setThreadsSortId(threadsSortId);
        threadInfo.setWriter(writer);
        threadInfo.setWriterId(writerId);
        threadInfo.setIsupload(0);
        threadInfo.setStatus("0");
        
        ThreadInfo inserted = threadInfoService.insert(threadInfo);

        if (inserted != null && writerId != null) {
            userInfoService.incrementThreadCount(writerId);
        }

        return APIResult.ok(inserted);
    }

    @DeleteMapping
    public APIResult remove(Integer id, Integer writerId) {
        threadInfoService.remove(id);
        if (writerId != null) {
            userInfoService.addExperience(writerId, -3);
        }
        return APIResult.ok("删除成功");
    }
}
