package com.henry.forum.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import com.henry.forum.admin.entity.UserUpload;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface UserUploadMapper {

	@Delete({ "delete from user_upload", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({ "insert into user_upload ( user_info_id, ", "upload)",
			"values ( #{userInfoId,jdbcType=INTEGER}, ", "#{upload,jdbcType=VARCHAR})" })
	//获取返回主键
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
	int insert(UserUpload row);

	@Select({ "select", "id, user_info_id, upload", "from user_upload", "where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "user_info_id", property = "userInfoId", jdbcType = JdbcType.INTEGER),
			@Result(column = "upload", property = "upload", jdbcType = JdbcType.VARCHAR) })
	UserUpload selectByPrimaryKey(Integer id);

	@Select({ "select", "id, user_info_id, upload", "from user_upload", "where user_info_id = #{user_info_id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "user_info_id", property = "userInfoId", jdbcType = JdbcType.INTEGER),
			@Result(column = "upload", property = "upload", jdbcType = JdbcType.VARCHAR) })
	List<UserUpload> selectByUserKey(Integer id);

	
	@Select({ "select", "id, user_info_id, upload", "from user_upload" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "user_info_id", property = "userInfoId", jdbcType = JdbcType.INTEGER),
			@Result(column = "upload", property = "upload", jdbcType = JdbcType.VARCHAR) })
	List<UserUpload> selectAll();

	@Update({ "update user_upload", "set user_info_id = #{userInfoId,jdbcType=INTEGER},",
			"upload = #{upload,jdbcType=VARCHAR}", "where id = #{id,jdbcType=INTEGER}" })
	int updateByPrimaryKey(UserUpload row);
	
}
