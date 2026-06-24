package com.battery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("battery_operation_record")
public class OperationRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String fileName;

    private String filePath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    private Integer status;

    private String remark;

    private Long reportId;
}
