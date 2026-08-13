package com.henry.forum.admin.mapper;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.ThreadLike;

@Mapper
public interface ThreadLikeMapper {
    @Insert("insert into thread_like (thread_info_id, user_info_id, create_time) values (#{threadInfoId}, #{userInfoId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ThreadLike like);

    @Delete("delete from thread_like where thread_info_id = #{threadInfoId} and user_info_id = #{userInfoId}")
    int delete(@Param("threadInfoId") Integer threadInfoId, @Param("userInfoId") Integer userInfoId);

    @Select("select count(*) from thread_like where thread_info_id = #{threadInfoId}")
    int countByThreadId(Integer threadInfoId);

    @Select("select * from thread_like where thread_info_id = #{threadInfoId} and user_info_id = #{userInfoId}")
    ThreadLike findByThreadAndUser(@Param("threadInfoId") Integer threadInfoId, @Param("userInfoId") Integer userInfoId);
}