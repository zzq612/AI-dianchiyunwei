package com.battery.service;

import com.battery.entity.BatteryData;
import com.battery.entity.PredictionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PredictionService {

    // 行业标准阈值
    private static final Integer SOH_REPLACE_THRESHOLD = 80; // SOH低于80%需更换
    private static final Double TEMP_WARN_THRESHOLD = 45.0; // 温度预警阈值
    private static final Double IR_CHANGE_WARN_THRESHOLD = 1.0; // 内阻变化率预警阈值
    private static final Double VOLTAGE_DIFF_WARN_THRESHOLD = 0.05; // 电压差预警阈值

    public PredictionResult predict(List<BatteryData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new RuntimeException("电池数据为空，无法进行预测");
        }

        log.info("开始预测分析，共{}条数据", dataList.size());
        
        PredictionResult result = new PredictionResult();
        BatteryData latestData = dataList.get(dataList.size() - 1);
        
        log.info("最新数据: SOH={}, SOC={}, maxIrChangeRate={}, maxTemp={}, voltageAvgDiff={}", 
            latestData.getSoh(), latestData.getSoc(), latestData.getMaxIrChangeRate(), 
            latestData.getMaxTemp(), latestData.getVoltageAvgDiff());
        
        result.setBatteryGroupId(latestData.getBatteryGroupId());
        result.setCellCount(latestData.getCellCount());
        result.setCurrentSoh(latestData.getSoh());
        result.setCurrentSoc(latestData.getSoc());
        result.setDataCount(dataList.size());
        result.setLatestDataTime(latestData.getTimestamp());
        
        log.info("设置基础信息后: currentSoh={}, currentSoc={}", result.getCurrentSoh(), result.getCurrentSoc());

        predictLife(dataList, result, latestData);

        predictFault(dataList, result, latestData);

        predictReplace(result, latestData);
        log.info("result：{}",result);
        return result;
    }

    // 寿命预测逻辑
    private void predictLife(List<BatteryData> dataList, PredictionResult result, BatteryData latestData) {
        Integer sohValue = latestData.getSoh();
        if (sohValue == null) {
            sohValue = 100;
            log.warn("SOH数据为空，使用默认值100%");
        }
        int currentSoh = sohValue;
        
        int totalCycleLife = 1500;
        int remainingCycle = (int) (totalCycleLife * (currentSoh / 100.0));
        result.setRemainingCycleLife(remainingCycle);

        double remainingDays = remainingCycle / 1.0;
        result.setRemainingUseDays(remainingDays);

        Double irChangeRateValue = latestData.getMaxIrChangeRate();
        if (irChangeRateValue == null) {
            irChangeRateValue = 0.0;
            log.warn("内阻变化率数据为空，使用默认值0.0");
        }
        double irChangeRate = irChangeRateValue;
        double decayRate = irChangeRate * 0.02;
        result.setSohDecayRate(decayRate);
        log.info("result:{}",result);
    }

    // 故障预测逻辑
    private void predictFault(List<BatteryData> dataList, PredictionResult result, BatteryData latestData) {
        List<String> faultTypes = new ArrayList<>();
        List<String> faultDetails = new ArrayList<>();
        int riskScore = 0;

        Integer sohValue = latestData.getSoh();
        int currentSoh = (sohValue != null) ? sohValue : 100;
        if (currentSoh < 60) {
            faultTypes.add("SOH严重不足");
            faultDetails.add(String.format("电池当前SOH仅%d%%，严重衰减，存在安全隐患", currentSoh));
            riskScore += 3;
        } else if (currentSoh < 70) {
            faultTypes.add("SOH偏低");
            faultDetails.add(String.format("电池当前SOH为%d%%，衰减明显，需密切关注", currentSoh));
            riskScore += 2;
        } else if (currentSoh < 80) {
            faultTypes.add("SOH下降");
            faultDetails.add(String.format("电池当前SOH为%d%%，已接近更换阈值", currentSoh));
            riskScore += 1;
        }

        Double maxTempValue = latestData.getMaxTemp();
        double maxTemp = (maxTempValue != null) ? maxTempValue : 0.0;
        if (maxTemp > TEMP_WARN_THRESHOLD) {
            faultTypes.add("温度异常");
            faultDetails.add(String.format("电池最高温度%.1f℃，超过预警阈值%.1f℃，存在热失控风险", maxTemp, TEMP_WARN_THRESHOLD));
            riskScore += 3;
        } else if (maxTemp > 40) {
            faultTypes.add("温度偏高");
            faultDetails.add(String.format("电池最高温度%.1f℃，处于偏高区间，需关注散热情况", maxTemp));
            riskScore += 1;
        }

        Double irChangeRateValue = latestData.getMaxIrChangeRate();
        double irChangeRate = (irChangeRateValue != null) ? irChangeRateValue : 0.0;
        if (irChangeRate > IR_CHANGE_WARN_THRESHOLD) {
            faultTypes.add("内阻突变");
            faultDetails.add(String.format("内阻最大变化率%.1f%%，超过预警阈值%.1f%%，电池内部老化严重", irChangeRate, IR_CHANGE_WARN_THRESHOLD));
            riskScore += 3;
        } else if (irChangeRate > 0.8) {
            faultTypes.add("内阻偏高");
            faultDetails.add(String.format("内阻最大变化率%.1f%%，处于偏高区间，需持续监控", irChangeRate));
            riskScore += 1;
        }

        Double voltageDiffValue = latestData.getVoltageAvgDiff();
        double voltageDiff = (voltageDiffValue != null) ? voltageDiffValue : 0.0;
        if (voltageDiff > VOLTAGE_DIFF_WARN_THRESHOLD) {
            faultTypes.add("电压不均衡");
            faultDetails.add(String.format("电池平均电压差%.3fV，超过预警阈值%.3fV，单体电池一致性差", voltageDiff, VOLTAGE_DIFF_WARN_THRESHOLD));
            riskScore += 2;
        } else if (voltageDiff > 0.04) {
            faultTypes.add("电压偏差偏大");
            faultDetails.add(String.format("电池平均电压差%.3fV，处于偏高区间，需关注单体电池状态", voltageDiff));
            riskScore += 1;
        }

        Integer currValue = latestData.getChargeDischargeCurr();
        int curr = (currValue != null) ? currValue : 0;
        if (curr > 100) {
            faultTypes.add("充放电电流过大");
            faultDetails.add(String.format("充放电电流%dA，超过安全阈值，存在过充过放风险", curr));
            riskScore += 2;
        }

        if (riskScore >= 5) {
            result.setFaultRiskLevel("高风险");
        } else if (riskScore >= 2) {
            result.setFaultRiskLevel("中风险");
        } else {
            result.setFaultRiskLevel("低风险");
        }

        result.setFaultTypes(faultTypes);
        result.setFaultDetails(faultDetails);
    }

    // 更换预测逻辑
    private void predictReplace(PredictionResult result, BatteryData latestData) {
        Integer sohValue = latestData.getSoh();
        int currentSoh = (sohValue != null) ? sohValue : 100;
        
        String riskLevel = result.getFaultRiskLevel();
        Double decayRateValue = result.getSohDecayRate();
        double decayRate = (decayRateValue != null && decayRateValue > 0) ? decayRateValue : 0.02;

        if (currentSoh < SOH_REPLACE_THRESHOLD || "高风险".equals(riskLevel)) {
            result.setReplaceSuggestion("强制更换");
            result.setReplaceDays(0.0);
            result.setReplaceReason(String.format("电池当前SOH为%d%%，已低于80%%的更换阈值，或存在高故障风险，建议立即更换", currentSoh));
        } else if (currentSoh < 85 || "中风险".equals(riskLevel)) {
            double daysToReplace = (currentSoh - SOH_REPLACE_THRESHOLD) / decayRate;
            result.setReplaceSuggestion("建议更换");
            result.setReplaceDays(daysToReplace);
            result.setReplaceReason(String.format("电池当前SOH为%d%%，处于衰减区间，预计%.0f天后将达到更换阈值，建议提前准备更换", currentSoh, daysToReplace));
        } else {
            double daysToReplace = (currentSoh - SOH_REPLACE_THRESHOLD) / decayRate;
            result.setReplaceSuggestion("正常使用");
            result.setReplaceDays(daysToReplace);
            result.setReplaceReason(String.format("电池当前SOH为%d%%，状态良好，预计%.0f天后将达到更换阈值，可正常使用并定期监控", currentSoh, daysToReplace));
        }
    }
}