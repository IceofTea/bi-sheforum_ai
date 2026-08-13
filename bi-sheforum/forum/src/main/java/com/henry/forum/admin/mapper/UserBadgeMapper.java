package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.henry.forum.admin.entity.BadgeType;
import com.henry.forum.admin.entity.UserBadge;

@Mapper
public interface UserBadgeMapper {
    @Insert("INSERT INTO user_badge (user_info_id, badge_type_id, is_show) VALUES (#{userInfoId}, #{badgeTypeId}, 1)")
    int insert(UserBadge userBadge);

    @Select("SELECT ub.id, ub.user_info_id as userInfoId, ub.badge_type_id as badgeTypeId, ub.earned_at as earnedAt, ub.is_show as isShow " +
            "FROM user_badge ub " +
            "WHERE ub.user_info_id = #{userInfoId} AND ub.is_show = 1 ORDER BY ub.earned_at DESC")
    List<UserBadge> selectByUserId(@Param("userInfoId") Integer userInfoId);

    @Select("SELECT * FROM badge_type WHERE id = #{badgeTypeId}")
    BadgeType selectBadgeTypeById(@Param("badgeTypeId") Integer badgeTypeId);

    @Select("SELECT COUNT(*) FROM user_badge WHERE user_info_id = #{userInfoId} AND badge_type_id = #{badgeTypeId}")
    int countByUserAndBadge(@Param("userInfoId") Integer userInfoId, @Param("badgeTypeId") Integer badgeTypeId);

    @Select("SELECT bt.* FROM badge_type bt WHERE bt.condition_type = #{conditionType} AND bt.condition_value <= #{currentValue}")
    List<BadgeType> selectBadgesToAward(@Param("conditionType") String conditionType, @Param("currentValue") Integer currentValue);
}