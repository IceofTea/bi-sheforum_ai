package com.henry.forum.admin.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.henry.forum.admin.entity.APIResult;
import com.henry.forum.admin.entity.UserMessage;
import com.henry.forum.admin.service.UserMessageService;

@RestController
@RequestMapping("/api/message")
public class UserMessageController {

    @Autowired
    private UserMessageService userMessageService;

    @PostMapping("/send")
    public APIResult sendMessage(@RequestParam Integer userId, @RequestParam(required = false) Integer fromUserId,
            @RequestParam String type, @RequestParam(required = false) Integer relatedId,
            @RequestParam String content) {
        int result = userMessageService.sendMessage(userId, fromUserId, type, relatedId, content);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(result > 0 ? 200 : 400);
        apiResult.setMsg(result > 0 ? "发送成功" : "发送失败");
        return apiResult;
    }

    @GetMapping("/list/{userId}")
    public APIResult getMessages(@PathVariable Integer userId) {
        List<UserMessage> list = userMessageService.getMessages(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(list);
        return apiResult;
    }

    @GetMapping("/unread/{userId}")
    public APIResult getUnreadMessages(@PathVariable Integer userId) {
        List<UserMessage> list = userMessageService.getUnreadMessages(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(list);
        return apiResult;
    }

    @GetMapping("/count/{userId}")
    public APIResult getUnreadCount(@PathVariable Integer userId) {
        int count = userMessageService.getUnreadCount(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setData(count);
        return apiResult;
    }

    @PostMapping("/readall/{userId}")
    public APIResult markAllRead(@PathVariable Integer userId) {
        int result = userMessageService.markAllRead(userId);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setMsg(result >= 0 ? "标记成功" : "标记失败");
        return apiResult;
    }

    @PostMapping("/read/{id}")
    public APIResult markRead(@PathVariable Integer id) {
        int result = userMessageService.markRead(id);
        APIResult apiResult = new APIResult();
        apiResult.setStatus(200);
        apiResult.setMsg(result > 0 ? "标记成功" : "标记失败");
        return apiResult;
    }
}