package com.henry.forum.admin.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.type.JdbcType;

public interface ThreadWriterMapper {
  
	@Delete({
        "delete from thread_writer",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
        "insert into thread_writer (id, thread_info_id, ",
        "wirter_id)",
        "values (#{id,jdbcType=INTEGER}, #{threadInfoId,jdbcType=INTEGER}, ",
        "#{wirterId,jdbcType=INTEGER})"
    })
    int insert(com.henry.forum.admin.entity.ThreadWriter row);

    @Select({
        "select",
        "id, thread_info_id, wirter_id",
        "from thread_writer",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="thread_info_id", property="threadInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="wirter_id", property="wirterId", jdbcType=JdbcType.INTEGER)
    })
    com.henry.forum.admin.entity.ThreadWriter selectByPrimaryKey(Integer id);

    @Select({
        "select",
        "id, thread_info_id, wirter_id",
        "from thread_writer"
    })
    @Results({
        @Result(column="id", property="id", jdbcType=JdbcType.INTEGER, id=true),
        @Result(column="thread_info_id", property="threadInfoId", jdbcType=JdbcType.INTEGER),
        @Result(column="wirter_id", property="wirterId", jdbcType=JdbcType.INTEGER)
    })
    List<com.henry.forum.admin.entity.ThreadWriter> selectAll();

    @Update({
        "update thread_writer",
        "set thread_info_id = #{threadInfoId,jdbcType=INTEGER},",
          "wirter_id = #{wirterId,jdbcType=INTEGER}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(com.henry.forum.admin.entity.ThreadWriter row);
}