package com.henry.forum.admin.mapper;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.CommentLike;

@Mapper
public interface CommentLikeMapper {
    @Insert("insert into comment_like (comment_id, user_info_id, create_time) values (#{commentId}, #{userInfoId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(CommentLike like);

    @Delete("delete from comment_like where comment_id = #{commentId} and user_info_id = #{userInfoId}")
    int delete(@Param("commentId") Integer commentId, @Param("userInfoId") Integer userInfoId);

    @Select("select count(*) from comment_like where comment_id = #{commentId}")
    int countByCommentId(Integer commentId);

    @Select("select * from comment_like where comment_id = #{commentId} and user_info_id = #{userInfoId}")
    CommentLike findByCommentAndUser(@Param("commentId") Integer commentId, @Param("userInfoId") Integer userInfoId);
}