package com.henry.forum.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.UserMessage;

@Mapper
public interface UserMessageMapper {
    @Insert("insert into user_message (user_id, from_user_id, type, related_id, content, is_read, create_time) values (#{userId}, #{fromUserId}, #{type}, #{relatedId}, #{content}, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserMessage message);

    @Select("select * from user_message where user_id = #{userId} order by create_time desc")
    List<UserMessage> getByUserId(Integer userId);

    @Select("select * from user_message where user_id = #{userId} and is_read = 0 order by create_time desc")
    List<UserMessage> getUnreadByUserId(Integer userId);

    @Select("select count(*) from user_message where user_id = #{userId} and is_read = 0")
    int countUnread(Integer userId);

    @Update("update user_message set is_read = 1 where user_id = #{userId}")
    int markAllRead(Integer userId);

    @Update("update user_message set is_read = 1 where id = #{id}")
    int markRead(Integer id);
}