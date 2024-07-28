package com.example.SmartIot.service.ifs;

import com.example.SmartIot.entity.History;

import java.time.LocalDate;
import java.util.List;

public interface HistoryService {

    List<History> getAllHistories();

    History createHistory(History history);

    List<History> getHistoriesByDeviceId(Long id);

    List<History> getHistoriesByEventType(String eventType);

    List<History> searchHistories(String deviceName, String deviceType, LocalDate startDate, LocalDate endDate, String roomArea);

}
