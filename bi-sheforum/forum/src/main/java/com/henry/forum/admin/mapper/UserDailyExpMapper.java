package com.henry.forum.admin.mapper;

import org.apache.ibatis.annotations.*;

import com.henry.forum.admin.entity.UserDailyExp;

@Mapper
public interface UserDailyExpMapper {
    @Insert("insert into user_daily_exp (user_info_id, exp_date, exp_got) values (#{userInfoId}, CURRENT_DATE, COALESCE(#{expGot}, 0))")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(UserDailyExp dailyExp);

    @Select("select * from user_daily_exp where user_info_id = #{userId} and exp_date = CURRENT_DATE")
    @Results({@Result(column = "exp_got", property = "expGot", jdbcType = org.apache.ibatis.type.JdbcType.INTEGER)})
    UserDailyExp getTodayByUserId(Integer userId);

    @Update("update user_daily_exp set exp_got = #{expGot} where user_info_id = #{userId} and exp_date = CURRENT_DATE")
    int updateTodayExp(@Param("userId") Integer userId, @Param("expGot") Integer expGot);
}