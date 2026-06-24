package com.battery.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.battery.entity.PredictionReport;
import com.battery.mapper.PredictionReportMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PredictionReportService extends ServiceImpl<PredictionReportMapper, PredictionReport> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Long savePredictionReport(com.battery.entity.PredictionResult result) throws JsonProcessingException {
        PredictionReport report = new PredictionReport();
        report.setBatteryGroupId(result.getBatteryGroupId());
        report.setCellCount(result.getCellCount());
        report.setCurrentSoh(result.getCurrentSoh());
        report.setCurrentSoc(result.getCurrentSoc());
        report.setRemainingCycleLife(result.getRemainingCycleLife());
        report.setRemainingUseDays(result.getRemainingUseDays());
        report.setSohDecayRate(result.getSohDecayRate());
        report.setFaultRiskLevel(result.getFaultRiskLevel());
        
        if (result.getFaultTypes() != null) {
            report.setFaultTypes(objectMapper.writeValueAsString(result.getFaultTypes()));
        }
        if (result.getFaultDetails() != null) {
            report.setFaultDetails(objectMapper.writeValueAsString(result.getFaultDetails()));
        }
        
        report.setReplaceSuggestion(result.getReplaceSuggestion());
        report.setReplaceDays(result.getReplaceDays());
        report.setReplaceReason(result.getReplaceReason());
        report.setDataCount(result.getDataCount());
        
        if (result.getLatestDataTime() != null) {
            report.setLatestDataTime(new java.sql.Timestamp(result.getLatestDataTime().getTime()).toLocalDateTime());
        }
        
        save(report);
        return report.getId();
    }

    public com.battery.entity.PredictionResult convertToResult(PredictionReport report) throws JsonProcessingException {
        com.battery.entity.PredictionResult result = new com.battery.entity.PredictionResult();
        result.setBatteryGroupId(report.getBatteryGroupId());
        result.setCellCount(report.getCellCount());
        result.setCurrentSoh(report.getCurrentSoh());
        result.setCurrentSoc(report.getCurrentSoc());
        result.setRemainingCycleLife(report.getRemainingCycleLife());
        result.setRemainingUseDays(report.getRemainingUseDays());
        result.setSohDecayRate(report.getSohDecayRate());
        result.setFaultRiskLevel(report.getFaultRiskLevel());
        
        if (report.getFaultTypes() != null) {
            result.setFaultTypes(objectMapper.readValue(report.getFaultTypes(), List.class));
        }
        if (report.getFaultDetails() != null) {
            result.setFaultDetails(objectMapper.readValue(report.getFaultDetails(), List.class));
        }
        
        result.setReplaceSuggestion(report.getReplaceSuggestion());
        result.setReplaceDays(report.getReplaceDays());
        result.setReplaceReason(report.getReplaceReason());
        result.setDataCount(report.getDataCount());
        
        if (report.getLatestDataTime() != null) {
            result.setLatestDataTime(java.sql.Timestamp.valueOf(report.getLatestDataTime()));
        }
        
        return result;
    }
}
