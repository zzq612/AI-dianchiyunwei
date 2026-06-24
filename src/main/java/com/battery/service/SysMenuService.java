package com.battery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.battery.entity.SysMenu;
import com.battery.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysMenuService extends ServiceImpl<SysMenuMapper, SysMenu> {

    public List<SysMenu> getMenuTreeByUserId(Long userId) {
        List<SysMenu> allMenus = baseMapper.selectMenusByUserId(userId);
        return buildMenuTree(allMenus);
    }

    public List<SysMenu> getAllMenuTree() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSortOrder);
        List<SysMenu> allMenus = list(wrapper);
        return buildMenuTree(allMenus);
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> menus) {
        List<SysMenu> rootMenus = menus.stream()
                .filter(menu -> menu.getParentId() == 0 || menu.getParentId() == null)
                .collect(Collectors.toList());

        for (SysMenu menu : rootMenus) {
            menu.setChildren(getChildren(menu.getId(), menus));
        }

        return rootMenus;
    }

    private List<SysMenu> getChildren(Long parentId, List<SysMenu> menus) {
        return menus.stream()
                .filter(menu -> menu.getParentId() != null && menu.getParentId().equals(parentId))
                .peek(menu -> menu.setChildren(getChildren(menu.getId(), menus)))
                .collect(Collectors.toList());
    }

    public void saveMenu(SysMenu menu) {
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        save(menu);
    }

    public void updateMenu(SysMenu menu) {
        menu.setUpdateTime(LocalDateTime.now());
        updateById(menu);
    }
}
