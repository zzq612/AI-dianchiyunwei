package com.battery.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserRoleMapper {

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(Long userId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int batchInsert(Long userId, List<Long> roleIds);
}
