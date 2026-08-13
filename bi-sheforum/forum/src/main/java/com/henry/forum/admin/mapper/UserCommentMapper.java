package com.henry.forum.admin.mapper;

import com.henry.forum.admin.entity.UserComment;
import java.util.List;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface UserCommentMapper {
    
    @Delete("delete from user_comment where id = #{id}")
    int deleteByPrimaryKey(Integer id);

    @Insert("insert into user_comment (user_info_id, thread_info_id, comment, time) values (#{userInfoId}, #{threadInfoId}, #{comment}, NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(UserComment row);

    @Select("select * from user_comment where id = #{id}")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="thread_info_id", property="threadInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP)
    })
    UserComment selectByPrimaryKey(Integer id);
    
    @Select("select * from user_comment where thread_info_id = #{threadId}")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfo", one = @One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="thread_info_id", property="threadInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="id", property="replyContent", many = @Many(select="com.henry.forum.admin.mapper.UserCommentReplyMapper.selectReplyAll",fetchType=FetchType.DEFAULT))
    })
    List<UserComment> selectByThreadId(Integer threadId);

    @Select("select * from user_comment")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfo", one = @One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="thread_info_id", property="threadInfo", one = @One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP)
    })
    List<UserComment> selectAll();

    @Update("update user_comment set comment = #{comment} where id = #{id}")
    int updateByPrimaryKey(UserComment row);

    @Select("select count(*) from user_comment where thread_info_id = #{threadId}")
    int countByThreadId(Integer threadId);
    
    @Select("select * from user_comment where user_info_id = #{userId} order by time desc")
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="user_info_id", property="userInfo", one = @One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="thread_info_id", property="threadInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="thread_info_id", property="threadInfo", one = @One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
        @Result(column="comment", property="comment", jdbcType=JdbcType.VARCHAR),
        @Result(column="time", property="time", jdbcType=JdbcType.TIMESTAMP)
    })
    List<UserComment> selectByUserId(Integer userId);
}