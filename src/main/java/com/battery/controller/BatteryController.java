package com.battery.controller;

import com.battery.entity.BatteryData;
import com.battery.entity.PredictionResult;
import com.battery.service.ExcelParseService;
import com.battery.service.PredictionService;
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
    public PredictionResult predictApi(@RequestParam("file") MultipartFile file) throws Exception {
        List<BatteryData> dataList = excelParseService.parseExcel(file);
        return predictionService.predict(dataList);
    }
}