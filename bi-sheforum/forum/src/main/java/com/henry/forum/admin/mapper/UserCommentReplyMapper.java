package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.type.JdbcType;
import com.henry.forum.admin.entity.UserCommentReply;

@Mapper
public interface UserCommentReplyMapper {
    
    @Delete("delete from user_comment_reply where id = #{id}")
    int deleteByPrimaryKey(Integer id);
    
    @Delete("delete from user_comment_reply where user_comment_id = #{commentId}")
    int deleteByCommentId(Integer commentId);

    @Insert("insert into user_comment_reply (user_comment_id, user_info_id, comment, time) values (#{userCommentId}, #{userInfoId}, #{comment}, NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(UserCommentReply row);

    @Select("select * from user_comment_reply where id = #{id}")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfo", one = @One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="user_comment_id", property="userCommentId", jdbcType=JdbcType.INTEGER),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP)
    })
    UserCommentReply selectByPrimaryKey(Integer id);

    @Select("select * from user_comment_reply where user_comment_id = #{commentId}")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfo", one = @One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="user_comment_id", property="userCommentId", jdbcType=JdbcType.INTEGER),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP)
    })
    List<UserCommentReply> selectReplyAll(Integer commentId);
}