package com.henry.forum.admin.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import com.henry.forum.admin.entity.Root;

@Mapper
public interface RootMapper {

	@Delete({ "delete from root", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({ "insert into root (id, rootname, ", "password, head, ", "realname, status)",
			"values (#{id,jdbcType=INTEGER}, #{rootname,jdbcType=VARCHAR}, ",
			"#{password,jdbcType=VARCHAR}, #{head,jdbcType=VARCHAR}, ",
			"#{realname,jdbcType=VARCHAR}, #{status,jdbcType=INTEGER})" })
	int insert(Root row);

	@Select({ "select", "id, rootname, password, head, realname, status", "from root",
			"where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "rootname", property = "rootname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "realname", property = "realname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER) })
	Root selectByPrimaryKey(Integer id);

	@Select({ "select", "id, rootname, password, head, realname, status", "from root",
			"where rootname = #{rootname,jdbcType=VARCHAR} limit 1" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "rootname", property = "rootname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "realname", property = "realname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER) })
	Root selectByName(String name);

	@Select({ "select", "id, rootname, password, head, realname, status", "from root" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "rootname", property = "rootname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "head", property = "head", jdbcType = JdbcType.VARCHAR),
			@Result(column = "realname", property = "realname", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.INTEGER) })
	List<Root> selectAll();

	@Update({ "update root", "set rootname = #{rootname,jdbcType=VARCHAR},", "password = #{password,jdbcType=VARCHAR},",
			"head = #{head,jdbcType=VARCHAR},", "realname = #{realname,jdbcType=VARCHAR},",
			"status = #{status,jdbcType=INTEGER}", "where id = #{id,jdbcType=INTEGER}" })
	int updateByPrimaryKey(Root row);

}