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

@Mapper
public interface ThreadContantMapper {
	
	//已下为旧�??
	@Delete({
        "delete from thread_contant",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into thread_contant (id, picture, ",
        "text)",
        "values (#{id,jdbcType=INTEGER}, #{picture,jdbcType=VARCHAR}, ",
        "#{text,jdbcType=VARCHAR})"
    })
    int insert(com.henry.forum.admin.entity.ThreadContant row);

    @Select({
        "select",
        "id, picture, text",
        "from thread_contant",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="picture", property="picture", jdbcType=JdbcType.VARCHAR),
        @Result(column="text", property="text", jdbcType=JdbcType.VARCHAR)
    })
    com.henry.forum.admin.entity.ThreadContant selectByPrimaryKey(Integer id);

    @Select({
        "select",
        "id, picture, text",
        "from thread_contant"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="picture", property="picture", jdbcType=JdbcType.VARCHAR),
        @Result(column="text", property="text", jdbcType=JdbcType.VARCHAR)
    })
    List<com.henry.forum.admin.entity.ThreadContant> selectAll();

    @Update({
        "update thread_contant",
        "set picture = #{picture,jdbcType=VARCHAR},",
          "text = #{text,jdbcType=VARCHAR}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(com.henry.forum.admin.entity.ThreadContant row);

}