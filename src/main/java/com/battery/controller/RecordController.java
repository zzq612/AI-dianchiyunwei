package com.battery.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.battery.entity.BatteryData;
import com.battery.entity.OperationRecord;
import com.battery.entity.PredictionResult;
import com.battery.entity.Result;
import com.battery.mapper.BatteryDataMapper;
import com.battery.service.OperationRecordService;
import com.battery.service.PredictionReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Autowired
    private OperationRecordService operationRecordService;

    @Autowired
    private PredictionReportService predictionReportService;

    @Autowired
    private BatteryDataMapper batteryDataMapper;

    @GetMapping("/page")
    public Result<Page<OperationRecord>> getRecordPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestHeader("Authorization") String token) {
        Page<OperationRecord> page = operationRecordService.getRecordPage(pageNum, pageSize, username);
        return Result.success(page);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        OperationRecord record = operationRecordService.getById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(record.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try {
            String encodedName = URLEncoder.encode(record.getFileName(), "UTF-8").replace("+", "%20");
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (Exception e) {
            log.error("下载文件失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/report/{id}")
    public Result<PredictionResult> getReport(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        OperationRecord record = operationRecordService.getById(id);
        if (record == null || record.getReportId() == null) {
            return Result.error(404, "记录或报告不存在");
        }

        try {
            com.battery.entity.PredictionReport report = predictionReportService.getById(record.getReportId());
            if (report == null) {
                return Result.error(404, "报告不存在");
            }
            PredictionResult result = predictionReportService.convertToResult(report);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取报告失败", e);
            return Result.error(500, "获取报告失败：" + e.getMessage());
        }
    }

    @GetMapping("/battery-data/{recordId}")
    public Result<Page<BatteryData>> getBatteryData(
            @PathVariable Long recordId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestHeader("Authorization") String token) {
        Page<BatteryData> page = new Page<>(pageNum, pageSize);
        Page<BatteryData> result = batteryDataMapper.selectPageByRecordId(page, recordId, "timestamp", "DESC");
        return Result.success(result);
    }
}
