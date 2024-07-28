package com.example.SmartIot.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;
import com.example.SmartIot.entity.History;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long>{

    List<History> findByDeviceIdAndEventTypeAndEventTimeBetween(Long deviceId, String eventType, LocalDateTime startTime, LocalDateTime endTime);

    //歷史紀錄搜尋欄位 日期、設備名稱、空間編號、設備類型
    @Query(value = "SELECT * FROM history h WHERE " +
           "(:deviceName IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(h.detail, '$.deviceName')) LIKE CONCAT('%', :deviceName, '%')) AND " +
           "(:deviceType IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(h.detail, '$.deviceType')) LIKE CONCAT('%', :deviceType, '%')) AND " +
           "(:startDate IS NULL OR DATE(h.event_time) >= :startDate) AND " +
           "(:endDate IS NULL OR DATE(h.event_time) <= :endDate) AND " +
           "(:roomArea IS NULL OR JSON_UNQUOTE(JSON_EXTRACT(h.detail, '$.roomArea')) LIKE CONCAT('%', :roomArea, '%'))",
           nativeQuery = true)
    List<History> searchHistories(@Param("deviceName") String deviceName,
                                  @Param("deviceType") String deviceType,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  @Param("roomArea") String roomArea);
}
