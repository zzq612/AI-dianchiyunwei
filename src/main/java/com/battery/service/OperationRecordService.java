package com.battery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.battery.entity.OperationRecord;
import com.battery.mapper.OperationRecordMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OperationRecordService extends ServiceImpl<OperationRecordMapper, OperationRecord> {

    @Value("${file.upload.path}")
    private String uploadPath;

    public String saveFile(MultipartFile file) throws IOException {
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        String fullPath = uploadPath + File.separator + storedName;
        file.transferTo(new File(fullPath));
        return fullPath;
    }

    public void saveRecord(String username, String originalFileName, String filePath, int status, String remark, Long reportId) {
        OperationRecord record = new OperationRecord();
        record.setUsername(username);
        record.setFileName(originalFileName);
        record.setFilePath(filePath);
        record.setUploadTime(LocalDateTime.now());
        record.setStatus(status);
        record.setRemark(remark);
        record.setReportId(reportId);
        save(record);
    }

    public Page<OperationRecord> getRecordPage(Integer pageNum, Integer pageSize, String username) {
        Page<OperationRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationRecord::getUsername, username);
        }
        wrapper.orderByDesc(OperationRecord::getUploadTime);
        return page(page, wrapper);
    }

    public Long getLatestRecordId(String username, String fileName) {
        LambdaQueryWrapper<OperationRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationRecord::getUsername, username)
               .eq(OperationRecord::getFileName, fileName)
               .orderByDesc(OperationRecord::getUploadTime)
               .last("LIMIT 1");
        OperationRecord record = getOne(wrapper);
        return record != null ? record.getId() : null;
    }
}
