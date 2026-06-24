package com.battery.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.battery.entity.OperationRecord;
import com.battery.entity.PredictionReport;
import com.battery.entity.Result;
import com.battery.service.OperationRecordService;
import com.battery.service.PredictionReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private OperationRecordService operationRecordService;

    @Autowired
    private PredictionReportService predictionReportService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        try {
            Map<String, Object> data = new HashMap<>();

            List<OperationRecord> records = operationRecordService.list();
            List<PredictionReport> reports = predictionReportService.list();

            long totalRecords = records.size();
            long totalReports = reports.size();
            long successRecords = records.stream().filter(r -> r.getStatus() != null && r.getStatus() == 1).count();

            double avgSoh = reports.stream()
                    .filter(r -> r.getCurrentSoh() != null)
                    .mapToInt(PredictionReport::getCurrentSoh)
                    .average().orElse(0);

            long highRiskCount = reports.stream()
                    .filter(r -> "高风险".equals(r.getFaultRiskLevel()))
                    .count();

            data.put("totalRecords", totalRecords);
            data.put("totalReports", totalReports);
            data.put("successRecords", successRecords);
            data.put("avgSoh", Math.round(avgSoh * 100) / 100.0);
            data.put("highRiskCount", highRiskCount);

            List<String> dates = new ArrayList<>();
            List<Long> uploadCounts = new ArrayList<>();
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                dates.add(date.toString().substring(5));
                LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
                long count = records.stream()
                        .filter(r -> r.getUploadTime() != null
                                && !r.getUploadTime().isBefore(start)
                                && !r.getUploadTime().isAfter(end))
                        .count();
                uploadCounts.add(count);
            }
            data.put("dates", dates);
            data.put("uploadCounts", uploadCounts);

            Map<String, Long> riskDistribution = reports.stream()
                    .filter(r -> r.getFaultRiskLevel() != null)
                    .collect(Collectors.groupingBy(PredictionReport::getFaultRiskLevel, LinkedHashMap::new, Collectors.counting()));
            if (!riskDistribution.containsKey("低风险")) riskDistribution.put("低风险", 0L);
            if (!riskDistribution.containsKey("中风险")) riskDistribution.put("中风险", 0L);
            if (!riskDistribution.containsKey("高风险")) riskDistribution.put("高风险", 0L);
            data.put("riskDistribution", riskDistribution);

            Map<String, Long> replaceDistribution = reports.stream()
                    .filter(r -> r.getReplaceSuggestion() != null)
                    .collect(Collectors.groupingBy(PredictionReport::getReplaceSuggestion, LinkedHashMap::new, Collectors.counting()));
            data.put("replaceDistribution", replaceDistribution);

            Map<String, Long> sohDistribution = new LinkedHashMap<>();
            sohDistribution.put("80-100%", 0L);
            sohDistribution.put("60-80%", 0L);
            sohDistribution.put("40-60%", 0L);
            sohDistribution.put("<40%", 0L);
            for (PredictionReport r : reports) {
                if (r.getCurrentSoh() == null) continue;
                int soh = r.getCurrentSoh();
                if (soh >= 80) sohDistribution.merge("80-100%", 1L, Long::sum);
                else if (soh >= 60) sohDistribution.merge("60-80%", 1L, Long::sum);
                else if (soh >= 40) sohDistribution.merge("40-60%", 1L, Long::sum);
                else sohDistribution.merge("<40%", 1L, Long::sum);
            }
            data.put("sohDistribution", sohDistribution);

            List<OperationRecord> recentRecords = records.stream()
                    .filter(r -> r.getUploadTime() != null)
                    .sorted((a, b) -> b.getUploadTime().compareTo(a.getUploadTime()))
                    .limit(10)
                    .collect(Collectors.toList());
            data.put("recentRecords", recentRecords);

            return Result.success(data);
        } catch (Exception e) {
            log.error("获取仪表盘统计数据失败", e);
            return Result.error(500, "获取统计数据失败：" + e.getMessage());
        }
    }
}
