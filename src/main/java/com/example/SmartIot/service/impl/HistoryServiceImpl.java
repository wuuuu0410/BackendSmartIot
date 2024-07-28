package com.example.SmartIot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.repository.HistoryRepository;
import com.example.SmartIot.service.ifs.HistoryService;
import java.util.List;
import java.time.LocalDate;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;

    @Autowired
    public HistoryServiceImpl(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @Override
    public List<History> getAllHistories() {
        return historyRepository.findAll();
    }

    @Override
    public History createHistory(History history) {
        return historyRepository.save(history);
    }

    @Override
    public List<History> getHistoriesByDeviceId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHistoriesByDeviceId'");
    }

    @Override
    public List<History> getHistoriesByEventType(String eventType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getHistoriesByEventType'");
    }

    //歷史紀錄搜尋欄位 日期、設備名稱、空間編號、設備類型
    @Override
    public List<History> searchHistories(String deviceName, String deviceType, LocalDate startDate, LocalDate endDate, String roomArea) {
        return historyRepository.searchHistories(deviceName, deviceType, startDate, endDate, roomArea);
    }

}
