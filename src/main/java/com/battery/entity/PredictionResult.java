package com.battery.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PredictionResult {
    // 基础信息
    private String batteryGroupId;
    private Integer cellCount;
    private Integer currentSoh;
    private Integer currentSoc;

    // 寿命预测结果
    private Integer remainingCycleLife; // 剩余循环次数
    private Double remainingUseDays;    // 剩余可使用天数
    private Double sohDecayRate;        // SOH日衰减率

    // 故障预测结果
    private String faultRiskLevel;       // 风险等级：低/中/高
    private List<String> faultTypes;     // 故障类型列表
    private List<String> faultDetails;   // 故障详情

    // 更换预测结果
    private String replaceSuggestion;    // 更换建议：强制更换/建议更换/正常使用
    private Double replaceDays;          // 预计更换天数
    private String replaceReason;        // 更换原因

    // 原始数据统计
    private Integer dataCount;
    private Date latestDataTime;
}