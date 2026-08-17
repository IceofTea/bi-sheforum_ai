package com.henry.forum.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.UserSign;

@Mapper
public interface UserSignMapper {
    @Insert("insert into user_sign (user_info_id, sign_date, points, continuous_days) values (#{userInfoId}, #{signDate}, #{points}, #{continuousDays})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserSign sign);

    @Select("select * from user_sign where user_info_id = #{userId} order by sign_date desc limit 1")
    UserSign getLatestByUserId(Integer userId);

    @Select("select * from user_sign where user_info_id = #{userId} order by sign_date desc")
    List<UserSign> getAllByUserId(Integer userId);

    @Select("select * from user_sign where user_info_id = #{userId} and DATE(sign_date) = CURRENT_DATE")
    UserSign getTodaySign(Integer userId);
}