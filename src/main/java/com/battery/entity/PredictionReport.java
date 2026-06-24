package com.battery.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("battery_prediction_report")
public class PredictionReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String batteryGroupId;

    private Integer cellCount;

    private Integer currentSoh;

    private Integer currentSoc;

    private Integer remainingCycleLife;

    private Double remainingUseDays;

    private Double sohDecayRate;

    private String faultRiskLevel;

    private String faultTypes;

    private String faultDetails;

    private String replaceSuggestion;

    private Double replaceDays;

    private String replaceReason;

    private Integer dataCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestDataTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
