package com.henry.forum.admin.mapper;

import com.henry.forum.admin.entity.Writer;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Many;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.type.JdbcType;
@Mapper
public interface WriterMapper {


	@Delete({
        "delete from writer",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into writer ( name, ",
        "birthday, sex, address, ",
        "introduction,head)",
        "values ( #{name,jdbcType=VARCHAR}, ",
        "#{birthday,jdbcType=VARCHAR}, #{sex,jdbcType=VARCHAR}, #{address,jdbcType=VARCHAR}, ",
        "#{introduction,jdbcType=VARCHAR},#{head,jdbcType=VARCHAR})"
    })
    int insert(Writer row);

    @Select({ "select", "*", "from writer", "where id = #{id,jdbcType=INTEGER}" })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="birthday", property="birthday", jdbcType=JdbcType.VARCHAR),
        @Result(column="sex", property="sex", jdbcType=JdbcType.VARCHAR),
        @Result(column="address", property="address", jdbcType=JdbcType.VARCHAR),
        @Result(column="head", property="head", jdbcType=JdbcType.VARCHAR),
        @Result(column="introduction", property="introduction", jdbcType=JdbcType.VARCHAR)
    })
    Writer selectByPrimaryKey(Integer id);

    @Select({ "select", "*", "from writer" })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="birthday", property="birthday", jdbcType=JdbcType.VARCHAR),
        @Result(column="sex", property="sex", jdbcType=JdbcType.VARCHAR),
        @Result(column="address", property="address", jdbcType=JdbcType.VARCHAR),
        @Result(column="introduction", property="introduction", jdbcType=JdbcType.VARCHAR),
        @Result(column="head", property="head", jdbcType=JdbcType.VARCHAR),
    })
    List<Writer> selectAll();

	@Update({ "<script>", 
		"update writer", 
		"<set>",
		"<if test='name!=null'> name=#{name},</if>",
		"<if test='birthday!=null'> birthday=#{birthday} ,</if>",
			"<if test='sex!=null'> sex=#{sex} ,</if>",
			"<if test='address!=null'> address=#{address},</if>",
			"<if test='introduction!=null'> introduction=#{introduction},</if>", 
			"<if test='head!=null'> head=#{head} ,</if>",
			"</set>", 
			"where id = #{id,jdbcType=INTEGER}", "</script>" })
    int updateByPrimaryKey(Writer row);
}