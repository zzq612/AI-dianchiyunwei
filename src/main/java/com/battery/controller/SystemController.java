package com.battery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.battery.entity.*;
import com.battery.service.SysMenuService;
import com.battery.service.SysRoleService;
import com.battery.service.SysUserService;
import com.battery.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/system")
public class SystemController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private JwtUtil jwtUtil;

    // ==================== 用户管理 ====================

    @GetMapping("/user/page")
    public Result<Page<SysUser>> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        Page<SysUser> page = sysUserService.getUserPage(pageNum, pageSize, username);
        return Result.success(page);
    }

    @PostMapping("/user")
    public Result<Void> saveUser(@RequestBody SysUser user, @RequestHeader("Authorization") String token) {
        sysUserService.saveUser(user);
        return Result.success(null);
    }

    @PutMapping("/user")
    public Result<Void> updateUser(@RequestBody SysUser user, @RequestHeader("Authorization") String token) {
        sysUserService.updateUser(user);
        return Result.success(null);
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        sysUserService.deleteUser(id);
        return Result.success(null);
    }

    @GetMapping("/user/role/{userId}")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        List<Long> roleIds = sysUserService.getRoleIdsByUserId(userId);
        return Result.success(roleIds);
    }

    @PostMapping("/user/role")
    public Result<Void> assignUserRoles(@RequestParam Long userId, @RequestBody List<Long> roleIds, @RequestHeader("Authorization") String token) {
        sysUserService.assignRoles(userId, roleIds);
        return Result.success(null);
    }

    // ==================== 角色管理 ====================

    @GetMapping("/role/page")
    public Result<Page<SysRole>> getRolePage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String roleName,
            @RequestHeader("Authorization") String token) {
        Page<SysRole> page = sysRoleService.getRolePage(pageNum, pageSize, roleName);
        return Result.success(page);
    }

    @GetMapping("/role/all")
    public Result<List<SysRole>> getAllRoles(@RequestHeader("Authorization") String token) {
        List<SysRole> roles = sysRoleService.getAllRoles();
        return Result.success(roles);
    }

    @PostMapping("/role")
    public Result<Void> saveRole(@RequestBody SysRole role, @RequestHeader("Authorization") String token) {
        sysRoleService.saveRole(role);
        return Result.success(null);
    }

    @PutMapping("/role")
    public Result<Void> updateRole(@RequestBody SysRole role, @RequestHeader("Authorization") String token) {
        sysRoleService.updateRole(role);
        return Result.success(null);
    }

    @DeleteMapping("/role/{id}")
    public Result<Void> deleteRole(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        sysRoleService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/role/menu/{roleId}")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long roleId, @RequestHeader("Authorization") String token) {
        List<Long> menuIds = sysRoleService.getMenuIdsByRoleId(roleId);
        return Result.success(menuIds);
    }

    @PostMapping("/role/menu")
    public Result<Void> assignRoleMenus(@RequestParam Long roleId, @RequestBody List<Long> menuIds, @RequestHeader("Authorization") String token) {
        sysRoleService.assignMenus(roleId, menuIds);
        return Result.success(null);
    }

    // ==================== 菜单管理 ====================

    @GetMapping("/menu/tree")
    public Result<List<SysMenu>> getMenuTree(@RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token);
        List<SysMenu> menuTree = sysMenuService.getMenuTreeByUserId(userId);
        return Result.success(menuTree);
    }

    @GetMapping("/menu/all")
    public Result<List<SysMenu>> getAllMenuTree(@RequestHeader("Authorization") String token) {
        List<SysMenu> menuTree = sysMenuService.getAllMenuTree();
        return Result.success(menuTree);
    }

    @PostMapping("/menu")
    public Result<Void> saveMenu(@RequestBody SysMenu menu, @RequestHeader("Authorization") String token) {
        sysMenuService.saveMenu(menu);
        return Result.success(null);
    }

    @PutMapping("/menu")
    public Result<Void> updateMenu(@RequestBody SysMenu menu, @RequestHeader("Authorization") String token) {
        sysMenuService.updateMenu(menu);
        return Result.success(null);
    }

    @DeleteMapping("/menu/{id}")
    public Result<Void> deleteMenu(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        sysMenuService.removeById(id);
        return Result.success(null);
    }
}
