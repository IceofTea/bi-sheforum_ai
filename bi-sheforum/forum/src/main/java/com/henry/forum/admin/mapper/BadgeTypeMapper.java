package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.henry.forum.admin.entity.BadgeType;

@Mapper
public interface BadgeTypeMapper {
    @Select("SELECT * FROM badge_type")
    List<BadgeType> selectAll();

    @Select("SELECT * FROM badge_type WHERE code = #{code}")
    BadgeType selectByCode(@Param("code") String code);

    @Select("SELECT * FROM badge_type WHERE category = #{category}")
    List<BadgeType> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM badge_type WHERE condition_type = #{conditionType} AND condition_value <= #{conditionValue}")
    List<BadgeType> selectAvailableBadges(@Param("conditionType") String conditionType, @Param("conditionValue") Integer conditionValue);

    @Select("SELECT * FROM badge_type WHERE id = #{id}")
    BadgeType selectById(@Param("id") Integer id);
}