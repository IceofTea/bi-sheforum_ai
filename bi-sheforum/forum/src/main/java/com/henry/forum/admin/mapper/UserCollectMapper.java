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

import com.henry.forum.admin.entity.UserCollect;

@Mapper
public interface UserCollectMapper {

	@Delete({ "delete from user_collect", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({ "insert into user_collect (thread_info_id, user_info_id, time)",
			"values (#{threadInfoId,jdbcType=INTEGER}, #{userInfoId,jdbcType=INTEGER}, NOW())" })
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
	int insert(UserCollect row);

	@Select({ "select", "*", "from user_collect", "where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "thread_info_id", property = "threadInfoId", jdbcType = JdbcType.INTEGER),
			@Result(column = "user_info_id", property = "userInfoId", jdbcType = JdbcType.INTEGER) })
	UserCollect selectByPrimaryKey(Integer id);

	@Select({ "select", "*", "from user_collect" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "thread_info_id", property = "threadInfo", one =
			@One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER)),
			@Result(column = "user_info_id", property = "userInfo", one =
			@One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER))
	})
	List<UserCollect> selectAll();

	@Select({ "select", "*", "from user_collect", "where user_info_id = #{user_info_id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "thread_info_id", property = "threadInfo", one =
			@One(select = "com.henry.forum.admin.mapper.ThreadInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER)),
			@Result(column = "user_info_id", property = "userInfo", one =
			@One(select = "com.henry.forum.admin.mapper.UserInfoMapper.selectByPrimaryKey",fetchType= FetchType.EAGER))
	})
	List<UserCollect> selectByUserKey(Integer id);
}