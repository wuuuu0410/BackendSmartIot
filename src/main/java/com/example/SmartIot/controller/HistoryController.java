package com.example.SmartIot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.service.ifs.HistoryService;

import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    @Autowired
    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    //取得所有歷史資料
    @GetMapping
    public List<History> getAllHistories() {
        return historyService.getAllHistories();
    }

    //電力模組使用 : 取回特定設備id的歷史紀錄
    @GetMapping("/for-power1")
    public List<History> getHistoriesByDeviceId(@PathVariable("id") Long id) {
        return historyService.getHistoriesByDeviceId(id);
    }

    //電力模組使用 : 取得設備開關狀態用
    @GetMapping("/for-power2")
    public List<History> getHistoriesByEventType(@RequestParam(name = "type",required = false) String eventType) {
        return historyService.getHistoriesByEventType(eventType);
    }

    @PostMapping
    public ResponseEntity<History> createHistory(@RequestBody History history) {
        try {
            // Set default values if not provided
            if (history.getEventTime() == null) {
                history.setEventTime(LocalDateTime.now());
            }
            if (history.getEventId() == null || history.getEventId().isEmpty()) {
                history.setEventId(UUID.randomUUID().toString());
            }

            History savedHistory = historyService.createHistory(history);
            return new ResponseEntity<>(savedHistory, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //歷史紀錄搜尋欄位 日期、設備名稱、空間編號、設備類型
    @GetMapping("/search")
    public List<History> searchHistories(
        @RequestParam(value = "deviceName", required = false) String deviceName,
        @RequestParam(value = "deviceType", required = false) String deviceType,
        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(value = "roomArea", required = false) String roomArea) {
        return historyService.searchHistories(deviceName, deviceType, startDate, endDate, roomArea);
    }

}