package com.battery.entity;

import lombok.Data;
import java.util.Date;

@Data
public class BatteryData {
    private Date timestamp;
    private String batteryGroupId;
    private Integer cellCount;
    private Double maxIrChangeRate;
    private Integer maxIrCellId;
    private Double remainingDischargeH;
    private Integer soh;
    private Integer soc;
    private Double avgTemp;
    private Double minTemp;
    private Integer minTempCellId;
    private Double maxTemp;
    private Integer maxTempCellId;
    private Double avgIr;
    private Double minIr;
    private Integer minIrCellId;
    private Double maxIr;
    private Integer maxIrCellId2;
    private Double voltageRange;
    private Double voltageAvgDiff;
    private Double avgVoltage;
    private Double minVoltage;
    private Integer minVoltageCellId;
    private Double maxVoltage;
    private Integer maxVoltageCellId;
    private Double ambientTemp1;
    private Double ambientTemp2;
    private Integer chargeDischargeCurr;
    private Double totalVoltage;
}