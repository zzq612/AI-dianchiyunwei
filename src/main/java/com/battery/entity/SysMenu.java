package com.battery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    
    private String menuName;
    
    private String menuPath;
    
    private String component;
    
    private String icon;
    
    private Integer sortOrder;
    
    private Integer menuType;
    
    private String permission;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableField(exist = false)
    private List<SysMenu> children;
}
