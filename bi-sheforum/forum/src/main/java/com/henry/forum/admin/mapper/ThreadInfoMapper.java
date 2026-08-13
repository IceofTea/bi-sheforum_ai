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
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.FetchType;
import org.apache.ibatis.type.JdbcType;

import com.henry.forum.admin.entity.ThreadInfo;

@Mapper
public interface ThreadInfoMapper {

	@Delete({ "delete from thread_info", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	@Insert({
			"insert into thread_info (threads_sort_id,picture,",
			"text, name, writer_id,writer,introduction,status,isupload",
			")",
			"values (#{threadsSortId,jdbcType=INTEGER}, ",
			"#{picture,jdbcType=VARCHAR}, #{text,jdbcType=VARCHAR}, ",
			"#{name,jdbcType=VARCHAR}, #{writerId,jdbcType=INTEGER}, ",
			"#{writer,jdbcType=VARCHAR}, #{introduction,jdbcType=VARCHAR}, ",
			"#{status,jdbcType=VARCHAR}, #{isupload,jdbcType=INTEGER})"
	})
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(ThreadInfo threadInfo);

	@Select({ "select", "*", "from thread_info", "where id = #{id,jdbcType=INTEGER}" })
	@Results({
			@Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer", property = "writer", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer_id", property = "writerId", jdbcType = JdbcType.INTEGER),
			@Result(column = "introduction", property = "introduction", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "isupload", property = "isupload", jdbcType = JdbcType.INTEGER),
			@Result(column = "picture", property = "picture", jdbcType = JdbcType.VARCHAR),
			@Result(column = "text", property = "text", jdbcType = JdbcType.VARCHAR),
			@Result(column = "threads_sort_id", property = "threadsSort", one = @One(select = "com.henry.forum.admin.mapper.ThreadsSortMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
	})
	ThreadInfo selectByPrimaryKey(Integer id);

	@Select({ "select", "*", "from thread_info", "where writer_id = #{writer_id,jdbcType=INTEGER}" })
	@Results({
			@Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "writer", property = "writer", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer_id", property = "writerId", jdbcType = JdbcType.INTEGER),
			@Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
			@Result(column = "introduction", property = "introduction", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "isupload", property = "isupload", jdbcType = JdbcType.INTEGER),
			@Result(column = "picture", property = "picture", jdbcType = JdbcType.VARCHAR),
			@Result(column = "text", property = "text", jdbcType = JdbcType.VARCHAR),
			@Result(column = "threads_sort_id", property = "threadsSort", one = @One(select = "com.henry.forum.admin.mapper.ThreadsSortMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
	})
	List<ThreadInfo> selectAllByWriter(Integer id);

	@Select({ "select", "*", "from thread_info", "where threads_sort_id = #{threads_sort_id,jdbcType=INTEGER}" })
	@Results({
			@Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "writer", property = "writer", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer_id", property = "writerId", jdbcType = JdbcType.INTEGER),
			@Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
			@Result(column = "introduction", property = "introduction", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "isupload", property = "isupload", jdbcType = JdbcType.INTEGER),
			@Result(column = "picture", property = "picture", jdbcType = JdbcType.VARCHAR),
			@Result(column = "text", property = "text", jdbcType = JdbcType.VARCHAR),
			@Result(column = "threads_sort_id", property = "threadsSort", one = @One(select = "com.henry.forum.admin.mapper.ThreadsSortMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
	})
	List<ThreadInfo> selectAllBySortId(Integer id);

	@Select({ "select", "*", "from thread_info" })
	@Results({
			@Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "writer", property = "writer", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer_id", property = "writerId", jdbcType = JdbcType.INTEGER),
			@Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
			@Result(column = "introduction", property = "introduction", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "isupload", property = "isupload", jdbcType = JdbcType.INTEGER),
			@Result(column = "picture", property = "picture", jdbcType = JdbcType.VARCHAR),
			@Result(column = "text", property = "text", jdbcType = JdbcType.VARCHAR),
			@Result(column = "threads_sort_id", property = "threadsSort", one = @One(select = "com.henry.forum.admin.mapper.ThreadsSortMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
	})
	List<ThreadInfo> selectAll();

	@Select({ "select", "*", "from thread_info", "where name = #{name}" })
	@Results({
			@Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "writer", property = "writer", jdbcType = JdbcType.VARCHAR),
			@Result(column = "writer_id", property = "writerId", jdbcType = JdbcType.INTEGER),
			@Result(column = "name", property = "name", jdbcType = JdbcType.VARCHAR),
			@Result(column = "introduction", property = "introduction", jdbcType = JdbcType.VARCHAR),
			@Result(column = "status", property = "status", jdbcType = JdbcType.VARCHAR),
			@Result(column = "isupload", property = "isupload", jdbcType = JdbcType.INTEGER),
			@Result(column = "picture", property = "picture", jdbcType = JdbcType.VARCHAR),
			@Result(column = "text", property = "text", jdbcType = JdbcType.VARCHAR),
			@Result(column = "threads_sort_id", property = "threadsSort", one = @One(select = "com.henry.forum.admin.mapper.ThreadsSortMapper.selectByPrimaryKey", fetchType = FetchType.EAGER)),
	})
	ThreadInfo selectByName(String name);

@Update({
			"<script>",
			"update thread_info",
			"<set>",
			"<if test='name != null'> name = #{name}, </if>",
			"<if test='introduction != null'> introduction = #{introduction}, </if>",
			"<if test='status != null'> status = #{status}, </if>",
			"<if test='isupload != null'> isupload = #{isupload}, </if>",
			"<if test='picture != null'> picture = #{picture}, </if>",
			"<if test='text != null'> text = #{text}, </if>",
			"</set>",
			"where id = #{id}",
			"</script>"
		})
	int updateByPrimaryKey(ThreadInfo threadInfo);

	@Select("select count(*) from thread_info where writer_id = #{writerId}")
	int countByWriterId(Integer writerId);
}