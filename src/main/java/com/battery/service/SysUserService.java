package com.battery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.battery.entity.SysUser;
import com.battery.mapper.SysUserMapper;
import com.battery.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    public SysUser findByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return getOne(wrapper);
    }

    public Page<SysUser> getUserPage(Integer pageNum, Integer pageSize, String username) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(SysUser::getUsername, username)
                   .or()
                   .like(SysUser::getNickname, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    public void saveUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);
    }

    public void updateUser(SysUser user) {
        SysUser existingUser = getById(user.getId());
        if (existingUser != null) {
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(existingUser.getPassword());
            }
            user.setUpdateTime(LocalDateTime.now());
            updateById(user);
        }
    }

    public void deleteUser(Long id) {
        removeById(id);
    }

    public List<Long> getRoleIdsByUserId(Long userId) {
        return sysUserRoleMapper.selectRoleIdsByUserId(userId);
    }

    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        sysUserRoleMapper.deleteByUserId(userId);
        if (roleIds != null && !roleIds.isEmpty()) {
            sysUserRoleMapper.batchInsert(userId, roleIds);
        }
    }
}
