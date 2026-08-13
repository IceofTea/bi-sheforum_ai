package com.henry.forum.admin.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.henry.forum.admin.entity.UserMessage;
import com.henry.forum.admin.mapper.UserMessageMapper;
import com.henry.forum.admin.service.UserMessageService;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Override
    public int sendMessage(Integer userId, Integer fromUserId, String type, Integer relatedId, String content) {
        if (userId == null) return 0;
        UserMessage message = new UserMessage();
        message.setUserId(userId);
        message.setFromUserId(fromUserId);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setContent(content);
        message.setIsRead(0);
        return userMessageMapper.insert(message);
    }

    @Override
    public List<UserMessage> getMessages(Integer userId) {
        return userId == null ? null : userMessageMapper.getByUserId(userId);
    }

    @Override
    public List<UserMessage> getUnreadMessages(Integer userId) {
        return userId == null ? null : userMessageMapper.getUnreadByUserId(userId);
    }

    @Override
    public int getUnreadCount(Integer userId) {
        return userId == null ? 0 : userMessageMapper.countUnread(userId);
    }

    @Override
    public int markAllRead(Integer userId) {
        return userId == null ? 0 : userMessageMapper.markAllRead(userId);
    }

    @Override
    public int markRead(Integer id) {
        return id == null ? 0 : userMessageMapper.markRead(id);
    }
}