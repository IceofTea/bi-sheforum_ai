package com.henry.forum.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.UserFollow;

@Mapper
public interface UserFollowMapper {
    @Insert("insert into user_follow (user_id, follow_user_id, create_time) values (#{userId}, #{followUserId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserFollow follow);

    @Delete("delete from user_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    int delete(@Param("userId") Integer userId, @Param("followUserId") Integer followUserId);

    @Select("select count(*) from user_follow where user_id = #{userId}")
    int countFollowings(Integer userId);

    @Select("select count(*) from user_follow where follow_user_id = #{followUserId}")
    int countFollowers(Integer followUserId);

    @Select("select * from user_follow where user_id = #{userId}")
    List<UserFollow> getFollowingList(Integer userId);

    @Select("select * from user_follow where follow_user_id = #{followUserId}")
    List<UserFollow> getFollowerList(Integer followUserId);

    @Select("select * from user_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    UserFollow find(@Param("userId") Integer userId, @Param("followUserId") Integer followUserId);
}