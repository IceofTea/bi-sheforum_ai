package com.henry.forum.admin.service;

import java.util.List;
import com.henry.forum.admin.entity.UserMessage;

public interface UserMessageService {
    int sendMessage(Integer userId, Integer fromUserId, String type, Integer relatedId, String content);
    List<UserMessage> getMessages(Integer userId);
    List<UserMessage> getUnreadMessages(Integer userId);
    int getUnreadCount(Integer userId);
    int markAllRead(Integer userId);
    int markRead(Integer id);
}