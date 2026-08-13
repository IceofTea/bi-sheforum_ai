package com.henry.forum.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.type.JdbcType;

import com.henry.forum.admin.entity.UserRead;
@Mapper
public interface UserReadMapper {

	@Delete({ "delete from user_read", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({ "insert into user_read (thread_info_id, user_info_id, time)",
			"values (#{threadInfoId,jdbcType=INTEGER}, #{userInfoId,jdbcType=INTEGER}, NOW())" })
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
	int insert(UserRead row);

	@Select({ "select", "*", "from user_read", "where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "thread_info_id", property = "threadInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER)),
			@Result(column = "user_info_id", property = "userInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER))
	})
	UserRead selectByPrimaryKey(Integer id);


	@Select({ "select", "*", "from user_read", "where user_info_id = #{user_info_id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "thread_info_id", property = "threadInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER)),
			@Result(column = "user_info_id", property = "userInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER))
	})
	List<UserRead> selectByUserKey(Integer id);
	
	@Select({ "select", "*", "from user_read" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "time", property = "time", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "thread_info_id", property = "threadInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER)),
			@Result(column = "user_info_id", property = "userInfo", one = 
			@One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER))
	})
	List<UserRead> selectAll();

	@Select({ "select count(*) from user_read", "where thread_info_id = #{thread_info_id,jdbcType=INTEGER}" })
	int countByThreadId(Integer threadInfoId);

	@Select("select count(distinct thread_info_id) from user_read where user_info_id = #{userInfoId}")
	int countUniqueByUserId(Integer userInfoId);
}