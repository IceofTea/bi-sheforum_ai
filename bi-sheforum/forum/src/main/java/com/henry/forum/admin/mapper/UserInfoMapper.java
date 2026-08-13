package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import com.henry.forum.admin.entity.UserInfo;

@Mapper
public interface UserInfoMapper {

	@Delete({ "delete from user_info", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({ "insert into user_info (username, password, head, nickname, phone, sex, realname, experience, level, points, thread_count, comment_count, login_count, continuous_login_days, last_login_time, register_time, status)",
			"values (#{username,jdbcType=VARCHAR}, ",
			"#{password,jdbcType=VARCHAR}, #{head,jdbcType=VARCHAR}, ",
			"#{nickname,jdbcType=VARCHAR}, ",
			"#{phone,jdbcType=VARCHAR}, ",
			"#{sex,jdbcType=VARCHAR}, ",
			"#{realname,jdbcType=VARCHAR}, ",
			"#{experience,jdbcType=INTEGER}, #{level,jdbcType=INTEGER}, #{points,jdbcType=INTEGER}, ",
			"#{threadCount,jdbcType=INTEGER}, #{commentCount,jdbcType=INTEGER}, #{loginCount,jdbcType=INTEGER}, #{continuousLoginDays,jdbcType=INTEGER}, #{lastLoginTime,jdbcType=VARCHAR}, #{registerTime,jdbcType=VARCHAR}, #{status,jdbcType=INTEGER})" })
	int insert(UserInfo row);

	@Select({ "select", "*", "from user_info",
			"where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "nickname", property = "nickname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "phone", property = "phone", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER),
			@Result(column = "experience", property = "experience", jdbcType = JdbcType.INTEGER),
			@Result(column = "level", property = "level", jdbcType = JdbcType.INTEGER),
			@Result(column = "points", property = "points", jdbcType = JdbcType.INTEGER),
			@Result(column = "thread_count", property = "threadCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "comment_count", property = "commentCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "login_count", property = "loginCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "continuous_login_days", property = "continuousLoginDays", jdbcType = JdbcType.INTEGER),
			@Result(column = "last_login_time", property = "lastLoginTime", jdbcType = JdbcType.VARCHAR),
			@Result(column = "register_time", property = "registerTime", jdbcType = JdbcType.VARCHAR) })
	UserInfo selectByPrimaryKey(Integer id);

	@Select({ "select", "*", "from user_info",
			"where username = #{username,jdbcType=VARCHAR} limit 1" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "nickname", property = "nickname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "phone", property = "phone", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER),
			@Result(column = "experience", property = "experience", jdbcType = JdbcType.INTEGER),
			@Result(column = "level", property = "level", jdbcType = JdbcType.INTEGER),
			@Result(column = "points", property = "points", jdbcType = JdbcType.INTEGER),
			@Result(column = "thread_count", property = "threadCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "comment_count", property = "commentCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "login_count", property = "loginCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "continuous_login_days", property = "continuousLoginDays", jdbcType = JdbcType.INTEGER),
			@Result(column = "last_login_time", property = "lastLoginTime", jdbcType = JdbcType.VARCHAR),
			@Result(column = "register_time", property = "registerTime", jdbcType = JdbcType.VARCHAR) })
	UserInfo selectByName(String name);

	@Select({ "select", "*", "from user_info", "order by experience desc" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "nickname", property = "nickname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "phone", property = "phone", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER),
			@Result(column = "experience", property = "experience", jdbcType = JdbcType.INTEGER),
			@Result(column = "level", property = "level", jdbcType = JdbcType.INTEGER),
			@Result(column = "points", property = "points", jdbcType = JdbcType.INTEGER),
			@Result(column = "thread_count", property = "threadCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "comment_count", property = "commentCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "login_count", property = "loginCount", jdbcType = JdbcType.INTEGER),
			@Result(column = "continuous_login_days", property = "continuousLoginDays", jdbcType = JdbcType.INTEGER),
			@Result(column = "last_login_time", property = "lastLoginTime", jdbcType = JdbcType.VARCHAR),
			@Result(column = "register_time", property = "registerTime", jdbcType = JdbcType.VARCHAR) })
	List<UserInfo> selectAll();

//
//	@Update({ "update user_info", "set username = #{username,jdbcType=VARCHAR},",
//			"password = #{password,jdbcType=VARCHAR},", "head = #{head,jdbcType=VARCHAR},",
//			"realname = #{realname,jdbcType=VARCHAR},", "phone = #{phone,jdbcType=VARCHAR},",
//			"status = #{status,jdbcType=INTEGER}", "where id = #{id,jdbcType=INTEGER}" })
	@Update({ "<script>", "update user_info", "<set>", "<if test='username!=null'> username=#{username},</if>",
			"<if test='password!=null'> password=#{password} ,</if>", "<if test='head!=null'> head=#{head},</if>",
			"<if test='nickname!=null'> nickname=#{nickname},</if>",
			"<if test='realname!=null'> realname=#{realname},</if>", "<if test='phone!=null'> phone=#{phone},</if>",
			"<if test='sex!=null'> sex=#{sex},</if>", "<if test='status!=null'> status=#{status},</if>",
			"<if test='experience!=null'> experience=#{experience},</if>",
			"<if test='level!=null'> level=#{level},</if>",
			"<if test='points!=null'> points=#{points},</if>",
			"<if test='threadCount!=null'> thread_count=#{threadCount},</if>",
			"<if test='commentCount!=null'> comment_count=#{commentCount},</if>",
			"<if test='loginCount!=null'> login_count=#{loginCount},</if>",
			"<if test='continuousLoginDays!=null'> continuous_login_days=#{continuousLoginDays},</if>",
			"<if test='lastLoginTime!=null'> last_login_time=#{lastLoginTime},</if>",
			"<if test='registerTime!=null'> register_time=#{registerTime},</if>",
			"</set>",
			"where id = #{id,jdbcType=INTEGER}", "</script>" })
	int updateByPrimaryKey(UserInfo row);

	@Update("update user_info set thread_count = thread_count + 1 where id = #{id}")
	int updateThreadCount(Integer id);

	@Update("update user_info set comment_count = comment_count + 1 where id = #{id}")
	int updateCommentCount(Integer id);

	@Update("update user_info set continuous_login_days = #{days} where id = #{userId}")
	int updateContinuousLoginDays(@Param("userId") Integer userId, @Param("days") Integer days);
}