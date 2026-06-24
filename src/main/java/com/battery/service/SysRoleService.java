package com.battery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.battery.entity.SysRole;
import com.battery.mapper.SysRoleMapper;
import com.battery.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysRoleService extends ServiceImpl<SysRoleMapper, SysRole> {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    public Page<SysRole> getRolePage(Integer pageNum, Integer pageSize, String roleName) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(SysRole::getRoleName, roleName);
        }
        wrapper.orderByDesc(SysRole::getCreateTime);
        return page(page, wrapper);
    }

    public List<SysRole> getAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getStatus, 1);
        return list(wrapper);
    }

    public void saveRole(SysRole role) {
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        save(role);
    }

    public void updateRole(SysRole role) {
        role.setUpdateTime(LocalDateTime.now());
        updateById(role);
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null && !menuIds.isEmpty()) {
            sysRoleMenuMapper.batchInsert(roleId, menuIds);
        }
    }
}
