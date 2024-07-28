package com.example.SmartIot.service.ifs;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PowerService {

    //特定設備在特定日期的耗電量,以分鐘為單位
    double calculateDevicePowerConsumption(Long deviceId, LocalDate date);

    //特定房間特定日期的總耗電量
    double calculateRoomDailyPowerConsumption(Long roomId, LocalDate date);

    // 特定房間特定月份的每日耗電量
    Map<String, Double> calculateRoomMonthlyPowerConsumption(Long roomId, int year, int month);

    //所有房間的特定日期 耗電量
    List<Map<String, Object>> calculateTotalDailyPowerConsumption(LocalDate date);

    //特定月的每日耗電量
    Map<String, Double> calculateMonthlyPowerConsumption(int year, int month);

    //特定年份每個月的耗電量
    Map<String, Double> calculateYearlyPowerConsumption(int year);

    //特定房間一年中每个月的用電量
    Map<String, Double> calculateRoomYearlyPowerConsumption(Long roomId, int year);

    //特定月份每個房間總耗電量
    List<Map<String, Object>> calculateMonthlyRoomPowerConsumption(int year, int month);
}
