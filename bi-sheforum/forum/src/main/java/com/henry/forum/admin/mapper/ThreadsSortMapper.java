package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.One;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

import com.henry.forum.admin.entity.ThreadsSort;

@Mapper
public interface ThreadsSortMapper {


	@Delete({
        "delete from threads_sort",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into threads_sort ( name, ",
        "picture, parent, ",
        "status)",
        "values ( #{name,jdbcType=VARCHAR}, ",
        "#{picture,jdbcType=VARCHAR}, #{parent,jdbcType=INTEGER}, ",
        "#{status,jdbcType=INTEGER})"
    })
    int insert(ThreadsSort row);

    @Select({ "select", "*", "from threads_sort", "where id = #{id,jdbcType=INTEGER}" })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="picture", property="picture", jdbcType=JdbcType.VARCHAR),
        @Result(column="parent", property="parent", jdbcType=JdbcType.INTEGER),
        @Result(column="status", property="status", jdbcType=JdbcType.INTEGER)
    })
    ThreadsSort selectByPrimaryKey(Integer id);

    @Select({ "select", "*", "from threads_sort" })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        @Result(column="picture", property="picture", jdbcType=JdbcType.VARCHAR),
        @Result(column="parent", property="parent", jdbcType=JdbcType.INTEGER),
        @Result(column="status", property="status", jdbcType=JdbcType.INTEGER),
    })
    List<ThreadsSort> selectAll();


	@Update({ "<script>",
		"update threads_sort",
		"<set>",
		"<if test='name!=null'> name=#{name},</if>",
		"<if test='picture!=null'> picture=#{picture} ,</if>",
			"<if test='parent!=null'> parent=#{parent} ,</if>",
			"<if test='status!=null'> status=#{status},</if>",
			"</set>",
			"where id = #{id,jdbcType=INTEGER}", "</script>" })
    int updateByPrimaryKey(ThreadsSort row);
}