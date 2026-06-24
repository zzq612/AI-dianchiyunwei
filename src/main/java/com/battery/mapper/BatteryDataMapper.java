package com.battery.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.battery.entity.BatteryData;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BatteryDataMapper {

    @Select("SELECT * FROM battery_data WHERE record_id = #{recordId} ORDER BY timestamp ASC")
    List<BatteryData> selectByRecordId(@Param("recordId") Long recordId);

    @Select("<script>" +
            "SELECT * FROM battery_data WHERE record_id = #{recordId} " +
            "<if test='orderBy != null and orderBy != \"\"'>" +
            "ORDER BY ${orderBy} ${orderDir} " +
            "</if>" +
            "</script>")
    Page<BatteryData> selectPageByRecordId(Page<BatteryData> page, 
                                           @Param("recordId") Long recordId,
                                           @Param("orderBy") String orderBy,
                                           @Param("orderDir") String orderDir);

    @Insert("<script>" +
            "INSERT INTO battery_data (record_id, timestamp, battery_group_id, cell_count, " +
            "max_ir_change_rate, max_ir_cell_id, remaining_discharge_h, soh, soc, " +
            "avg_temp, min_temp, min_temp_cell_id, max_temp, max_temp_cell_id, " +
            "avg_ir, min_ir, min_ir_cell_id, max_ir, max_ir_cell_id_2, " +
            "voltage_range, voltage_avg_diff, avg_voltage, " +
            "min_voltage, min_voltage_cell_id, max_voltage, max_voltage_cell_id, " +
            "ambient_temp_1, ambient_temp_2, charge_discharge_curr, total_voltage) VALUES " +
            "<foreach collection='dataList' item='item' separator=','>" +
            "(#{recordId}, #{item.timestamp}, #{item.batteryGroupId}, #{item.cellCount}, " +
            "#{item.maxIrChangeRate}, #{item.maxIrCellId}, #{item.remainingDischargeH}, #{item.soh}, #{item.soc}, " +
            "#{item.avgTemp}, #{item.minTemp}, #{item.minTempCellId}, #{item.maxTemp}, #{item.maxTempCellId}, " +
            "#{item.avgIr}, #{item.minIr}, #{item.minIrCellId}, #{item.maxIr}, #{item.maxIrCellId2}, " +
            "#{item.voltageRange}, #{item.voltageAvgDiff}, #{item.avgVoltage}, " +
            "#{item.minVoltage}, #{item.minVoltageCellId}, #{item.maxVoltage}, #{item.maxVoltageCellId}, " +
            "#{item.ambientTemp1}, #{item.ambientTemp2}, #{item.chargeDischargeCurr}, #{item.totalVoltage})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("recordId") Long recordId, @Param("dataList") List<BatteryData> dataList);
}
