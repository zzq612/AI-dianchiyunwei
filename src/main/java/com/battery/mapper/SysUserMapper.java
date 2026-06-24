package com.battery.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.battery.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    @Select("SELECT u.* FROM sys_user u " +
            "JOIN sys_user_role ur ON u.id = ur.user_id " +
            "JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE r.role_code = #{roleCode}")
    List<SysUser> selectUsersByRoleCode(String roleCode);
}
