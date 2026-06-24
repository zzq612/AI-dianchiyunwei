package com.battery.service;

import com.battery.entity.BatteryData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ExcelParseService {

    public List<BatteryData> parseExcel(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new RuntimeException("文件名不能为空");
        }

        if (fileName.toLowerCase().endsWith(".csv")) {
            return parseCsv(file);
        } else {
            return parseExcelFile(file);
        }
    }

    private List<BatteryData> parseCsv(MultipartFile file) throws Exception {
        List<BatteryData> dataList = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"));
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new RuntimeException("CSV文件为空");
            }
            
            headerLine = headerLine.replace("\uFEFF", "");
            
            String[] headers = headerLine.split(",");
            java.util.Map<String, Integer> colIndexMap = new java.util.HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim().toLowerCase();
                colIndexMap.put(header, i);
                log.info("列[{}]: '{}' -> 索引{}", i, header, i);
            }
            
            log.info("CSV表头解析完成，共{}列: {}", headers.length, colIndexMap.keySet());

            int rowNum = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] values = line.split(",", -1);
                
                if (rowNum == 0) {
                    log.info("第一行数据共{}列，前5列值: [{}]", 
                        values.length, 
                        String.join(", ", 
                            values.length > 0 ? values[0] : "",
                            values.length > 1 ? values[1] : "",
                            values.length > 2 ? values[2] : "",
                            values.length > 3 ? values[3] : "",
                            values.length > 4 ? values[4] : ""
                        )
                    );
                }
                
                BatteryData data = new BatteryData();
                
                Integer sohIndex = colIndexMap.get("soh");
                Integer socIndex = colIndexMap.get("soc");
                
                if (rowNum == 0) {
                    log.info("SOH列索引: {}, SOC列索引: {}", sohIndex, socIndex);
                    if (sohIndex != null && sohIndex < values.length) {
                        log.info("SOH原始值: '{}'", values[sohIndex]);
                    }
                    if (socIndex != null && socIndex < values.length) {
                        log.info("SOC原始值: '{}'", values[socIndex]);
                    }
                }
                
                data.setTimestamp(parseCsvDate(values, colIndexMap.get("timestamp")));
                data.setBatteryGroupId(getCsvValue(values, colIndexMap.get("battery_group_id")));
                data.setCellCount(getCsvIntValue(values, colIndexMap.get("cell_count")));
                data.setMaxIrChangeRate(getCsvDoubleValue(values, colIndexMap.get("max_ir_change_rate")));
                data.setMaxIrCellId(getCsvIntValue(values, colIndexMap.get("max_ir_cell_id")));
                data.setRemainingDischargeH(getCsvDoubleValue(values, colIndexMap.get("remaining_discharge_h")));
                data.setSoh(getCsvIntValue(values, sohIndex));
                data.setSoc(getCsvIntValue(values, socIndex));
                data.setAvgTemp(getCsvDoubleValue(values, colIndexMap.get("avg_temp")));
                data.setMinTemp(getCsvDoubleValue(values, colIndexMap.get("min_temp")));
                data.setMinTempCellId(getCsvIntValue(values, colIndexMap.get("min_temp_cell_id")));
                data.setMaxTemp(getCsvDoubleValue(values, colIndexMap.get("max_temp")));
                data.setMaxTempCellId(getCsvIntValue(values, colIndexMap.get("max_temp_cell_id")));
                data.setAvgIr(getCsvDoubleValue(values, colIndexMap.get("avg_ir")));
                data.setMinIr(getCsvDoubleValue(values, colIndexMap.get("min_ir")));
                data.setMinIrCellId(getCsvIntValue(values, colIndexMap.get("min_ir_cell_id")));
                data.setMaxIr(getCsvDoubleValue(values, colIndexMap.get("max_ir")));
                data.setMaxIrCellId2(getCsvIntValue(values, colIndexMap.get("max_ir_cell_id.1")));
                data.setVoltageRange(getCsvDoubleValue(values, colIndexMap.get("voltage_range")));
                data.setVoltageAvgDiff(getCsvDoubleValue(values, colIndexMap.get("voltage_avg_diff")));
                data.setAvgVoltage(getCsvDoubleValue(values, colIndexMap.get("avg_voltage")));
                data.setMinVoltage(getCsvDoubleValue(values, colIndexMap.get("min_voltage")));
                data.setMinVoltageCellId(getCsvIntValue(values, colIndexMap.get("min_voltage_cell_id")));
                data.setMaxVoltage(getCsvDoubleValue(values, colIndexMap.get("max_voltage")));
                data.setMaxVoltageCellId(getCsvIntValue(values, colIndexMap.get("max_voltage_cell_id")));
                data.setAmbientTemp1(getCsvDoubleValue(values, colIndexMap.get("ambient_temp_1")));
                data.setAmbientTemp2(getCsvDoubleValue(values, colIndexMap.get("ambient_temp_2")));
                data.setChargeDischargeCurr(getCsvIntValue(values, colIndexMap.get("charge_discharge_curr")));
                data.setTotalVoltage(getCsvDoubleValue(values, colIndexMap.get("total_voltage")));
                
                rowNum++;
                if (rowNum <= 3) {
                    log.info("第{}行数据: SOH={}, SOC={}, maxIrChangeRate={}", 
                        rowNum, data.getSoh(), data.getSoc(), data.getMaxIrChangeRate());
                }
                
                dataList.add(data);
            }
            
            log.info("CSV解析完成，共读取 {} 条数据", dataList.size());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        
        return dataList;
    }

    private String getCsvValue(String[] values, Integer index) {
        if (index == null || index >= values.length) return null;
        String value = values[index].trim();
        return value.isEmpty() ? null : value;
    }

    private Integer getCsvIntValue(String[] values, Integer index) {
        String value = getCsvValue(values, index);
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                return (int) Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                log.warn("无法解析整数值: '{}', 返回null", value);
                return null;
            }
        }
    }

    private Double getCsvDoubleValue(String[] values, Integer index) {
        String value = getCsvValue(values, index);
        if (value == null) return null;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Date parseCsvDate(String[] values, Integer index) {
        String value = getCsvValue(values, index);
        if (value == null) return null;
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
        } catch (Exception e) {
            try {
                return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(value);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private List<BatteryData> parseExcelFile(MultipartFile file) throws Exception {
        List<BatteryData> dataList = new ArrayList<>();
        InputStream inputStream = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        // 读取表头，映射列索引
        Row headerRow = sheet.getRow(0);
        int colCount = headerRow.getLastCellNum();
        java.util.Map<String, Integer> colIndexMap = new java.util.HashMap<>();
        for (int i = 0; i < colCount; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null) {
                String colName = cell.getStringCellValue().trim();
                colIndexMap.put(colName, i);
            }
        }

        // 读取数据行
        for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;
            BatteryData data = new BatteryData();

            // 填充字段
            data.setTimestamp(getCellDateValue(row, colIndexMap.get("timestamp")));
            data.setBatteryGroupId(getCellStringValue(row, colIndexMap.get("battery_group_id")));
            data.setCellCount(getCellIntValue(row, colIndexMap.get("cell_count")));
            data.setMaxIrChangeRate(getCellDoubleValue(row, colIndexMap.get("max_ir_change_rate")));
            data.setMaxIrCellId(getCellIntValue(row, colIndexMap.get("max_ir_cell_id")));
            data.setRemainingDischargeH(getCellDoubleValue(row, colIndexMap.get("remaining_discharge_h")));
            data.setSoh(getCellIntValue(row, colIndexMap.get("soh")));
            data.setSoc(getCellIntValue(row, colIndexMap.get("soc")));
            data.setAvgTemp(getCellDoubleValue(row, colIndexMap.get("avg_temp")));
            data.setMinTemp(getCellDoubleValue(row, colIndexMap.get("min_temp")));
            data.setMinTempCellId(getCellIntValue(row, colIndexMap.get("min_temp_cell_id")));
            data.setMaxTemp(getCellDoubleValue(row, colIndexMap.get("max_temp")));
            data.setMaxTempCellId(getCellIntValue(row, colIndexMap.get("max_temp_cell_id")));
            data.setAvgIr(getCellDoubleValue(row, colIndexMap.get("avg_ir")));
            data.setMinIr(getCellDoubleValue(row, colIndexMap.get("min_ir")));
            data.setMinIrCellId(getCellIntValue(row, colIndexMap.get("min_ir_cell_id")));
            data.setMaxIr(getCellDoubleValue(row, colIndexMap.get("max_ir")));
            data.setMaxIrCellId2(getCellIntValue(row, colIndexMap.get("max_ir_cell_id.1")));
            data.setVoltageRange(getCellDoubleValue(row, colIndexMap.get("voltage_range")));
            data.setVoltageAvgDiff(getCellDoubleValue(row, colIndexMap.get("voltage_avg_diff")));
            data.setAvgVoltage(getCellDoubleValue(row, colIndexMap.get("avg_voltage")));
            data.setMinVoltage(getCellDoubleValue(row, colIndexMap.get("min_voltage")));
            data.setMinVoltageCellId(getCellIntValue(row, colIndexMap.get("min_voltage_cell_id")));
            data.setMaxVoltage(getCellDoubleValue(row, colIndexMap.get("max_voltage")));
            data.setMaxVoltageCellId(getCellIntValue(row, colIndexMap.get("max_voltage_cell_id")));
            data.setAmbientTemp1(getCellDoubleValue(row, colIndexMap.get("ambient_temp_1")));
            data.setAmbientTemp2(getCellDoubleValue(row, colIndexMap.get("ambient_temp_2")));
            data.setChargeDischargeCurr(getCellIntValue(row, colIndexMap.get("charge_discharge_curr")));
            data.setTotalVoltage(getCellDoubleValue(row, colIndexMap.get("total_voltage")));

            dataList.add(data);
        }
        log.info("Excel解析完成，共读取 {} 条数据", dataList.size());


        workbook.close();
        inputStream.close();
        return dataList;
    }

    // 工具方法：获取单元格字符串值
    private String getCellStringValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    // 工具方法：获取单元格整数值
    private Integer getCellIntValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.NUMERIC);
        return (int) cell.getNumericCellValue();
    }

    // 工具方法：获取单元格浮点值
    private Double getCellDoubleValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        cell.setCellType(CellType.NUMERIC);
        return cell.getNumericCellValue();
    }

    // 工具方法：获取单元格日期值
    private Date getCellDateValue(Row row, Integer colIndex) {
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;
        try {
            return cell.getDateCellValue();
        } catch (Exception e) {
            // 处理字符串格式的日期
            String dateStr = getCellStringValue(row, colIndex);
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStr);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}