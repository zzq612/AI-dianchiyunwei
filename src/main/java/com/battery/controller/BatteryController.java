package com.battery.controller;

import com.battery.entity.BatteryData;
import com.battery.entity.PredictionResult;
import com.battery.entity.Result;
import com.battery.service.ExcelParseService;
import com.battery.service.OperationRecordService;
import com.battery.service.PredictionReportService;
import com.battery.service.PredictionService;
import com.battery.util.JwtUtil;
import com.battery.mapper.BatteryDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Controller
public class BatteryController {

    @Autowired
    private ExcelParseService excelParseService;

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private OperationRecordService operationRecordService;

    @Autowired
    private PredictionReportService predictionReportService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BatteryDataMapper batteryDataMapper;

    // 首页
    @GetMapping("/")
    public String index() {
        return "index";
    }




    // 上传Excel并预测
    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // 1. 解析Excel
            log.info("开始解析excel");
            List<BatteryData> dataList = excelParseService.parseExcel(file);
            // 2. 执行预测
            log.info("执行预测");
            PredictionResult result = predictionService.predict(dataList);
            // 3. 返回结果
            log.info("返回结果");
            model.addAttribute("result", result);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("success", false);
            model.addAttribute("errorMsg", e.getMessage());
        }
        return "index";
    }

    // 接口：JSON格式返回预测结果（用于前后端分离）
    @PostMapping("/api/predict")
    @ResponseBody
    public Result<PredictionResult> predictApi(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String username = "unknown";
            if (token != null) {
                username = jwtUtil.getUsernameFromToken(token);
                if (username == null) username = "unknown";
            }

            String originalFileName = file.getOriginalFilename();

            List<BatteryData> dataList = excelParseService.parseExcel(file);
            PredictionResult result = predictionService.predict(dataList);

            String filePath = operationRecordService.saveFile(file);

            Long reportId = predictionReportService.savePredictionReport(result);
            operationRecordService.saveRecord(username, originalFileName, filePath, 1, "预测成功", reportId);

            Long recordId = operationRecordService.getLatestRecordId(username, originalFileName);
            if (recordId != null && !dataList.isEmpty()) {
                batteryDataMapper.batchInsert(recordId, dataList);
            }

            return Result.success(result);
        } catch (Exception e) {
            log.error("预测失败", e);
            try {
                String username = "unknown";
                if (token != null) {
                    username = jwtUtil.getUsernameFromToken(token);
                    if (username == null) username = "unknown";
                }
                operationRecordService.saveRecord(username, file.getOriginalFilename(), "", 0, "预测失败：" + e.getMessage(), null);
            } catch (Exception ex) {
                log.error("保存失败记录异常", ex);
            }
            return Result.error(500, "预测失败：" + e.getMessage());
        }
    }
}