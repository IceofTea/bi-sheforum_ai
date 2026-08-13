package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.henry.forum.admin.entity.ThreadsSort;

@Mapper
public interface UserSortMapper {
    
    @Insert("INSERT INTO user_sort_follow (user_id, sort_id) VALUES (#{userId}, #{sortId})")
    int followSort(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Delete("DELETE FROM user_sort_follow WHERE user_id = #{userId} AND sort_id = #{sortId}")
    int unfollowSort(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Select("SELECT s.* FROM threads_sort s INNER JOIN user_sort_follow f ON s.id = f.sort_id WHERE f.user_id = #{userId} ORDER BY f.create_time DESC")
    List<ThreadsSort> selectFollowedSorts(Integer userId);
    
    @Select("SELECT COUNT(*) FROM user_sort_follow WHERE user_id = #{userId} AND sort_id = #{sortId}")
    int isFollowed(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Insert("INSERT INTO user_sort_visit (user_id, sort_id, visit_type, visit_count) VALUES (#{userId}, #{sortId}, #{visitType}, #{visitCount})")
    int addVisit(@Param("userId") Integer userId, @Param("sortId") Integer sortId, @Param("visitType") Integer visitType, @Param("visitCount") Integer visitCount);
    
    @Select("SELECT s.* FROM threads_sort s INNER JOIN user_sort_visit v ON s.id = v.sort_id WHERE v.user_id = #{userId} GROUP BY s.id ORDER BY SUM(v.visit_count) DESC LIMIT #{limit}")
    List<ThreadsSort> selectMostVisitedSorts(@Param("userId") Integer userId, @Param("limit") Integer limit);
    
    @Insert("INSERT INTO user_sort_create (user_id, sort_id) VALUES (#{userId}, #{sortId})")
    int createSort(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Delete("DELETE FROM user_sort_create WHERE user_id = #{userId} AND sort_id = #{sortId}")
    int deleteCreatedSort(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Select("SELECT s.* FROM threads_sort s INNER JOIN user_sort_create c ON s.id = c.sort_id WHERE c.user_id = #{userId} ORDER BY c.create_time DESC")
    List<ThreadsSort> selectCreatedSorts(Integer userId);
    
    @Select("SELECT id FROM threads_sort WHERE parent = #{parentId}")
    List<Integer> selectChildSortIds(Integer parentId);
    
    @Select("SELECT COUNT(*) FROM user_sort_create WHERE user_id = #{userId} AND sort_id = #{sortId}")
    int isCreated(@Param("userId") Integer userId, @Param("sortId") Integer sortId);
    
    @Select("SELECT s.* FROM threads_sort s ORDER BY s.id ASC")
    List<ThreadsSort> selectAllSorts();
    
    @Insert("INSERT INTO threads_sort (name, parent) VALUES (#{name}, #{parent})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertSort(ThreadsSort sort);
    
    @Update("UPDATE threads_sort SET name = #{name}, introduction = #{introduction} WHERE id = #{id}")
    int updateSort(ThreadsSort sort);
    
    @Delete("DELETE FROM threads_sort WHERE id = #{id}")
    int deleteSort(Integer id);
    
    @Select("SELECT COUNT(*) FROM thread_info WHERE threads_sort_id = #{sortId}")
    int countThreadsBySort(Integer sortId);
}