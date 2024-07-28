package com.example.SmartIot.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.Duration;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.repository.HistoryRepository;
import com.example.SmartIot.repository.RoomRepository;
import com.example.SmartIot.service.ifs.PowerService;

import jakarta.transaction.Transactional;

@Service
public class PowerServiceImpl implements PowerService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private RoomRepository roomRepository;

    // 特定設備特定日期的用電量
    @Override
    @Transactional
    public double calculateDevicePowerConsumption(Long deviceId, LocalDate date) {

        // 起始時間和結束時間
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        // 查看歷史紀錄
        List<History> histories = historyRepository.findByDeviceIdAndEventTypeAndEventTimeBetween(
                deviceId, "設備開關", startOfDay, endOfDay);

        // 總使用電量
        double totalPowerConsumption = 0;
        // 最後一次開啟的時間,用來計算每次開啟和關閉的時長
        LocalDateTime lastOnTime = null;
        // 找尋該設備
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new IllegalArgumentException("設備未找到"));

        // 計算時長
        for (History history : histories) {
            if (history.getDetail().get("status").equals("開")) {
                lastOnTime = history.getEventTime();
            } else if (history.getDetail().get("status").equals("關") && lastOnTime != null) {
                Duration duration = Duration.between(lastOnTime, history.getEventTime());
                // 累積總使用時間 * 功率
                totalPowerConsumption += (duration.toMinutes() / 60.0) * device.getPowerConsumptionRate();
                lastOnTime = null;
            }
        }

        return totalPowerConsumption;
    }

    // 計算特定房間特定日期的設備消耗電量
    @Override
    @Transactional
    public double calculateRoomDailyPowerConsumption(Long roomId, LocalDate date) {
        List<Device> devices = deviceRepository.findByRoom_Id(roomId);

        return devices.parallelStream()
                .mapToDouble(device -> calculateDevicePowerConsumption(device.getId(), date))
                .sum();
    }

    @Override
    @Transactional
    public Map<String, Double> calculateRoomMonthlyPowerConsumption(Long roomId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return startOfMonth.datesUntil(endOfMonth.plusDays(1))
                .parallel()
                .collect(Collectors.toMap(
                        date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        date -> calculateRoomDailyPowerConsumption(roomId, date),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    // 所有房間加總的耗電量
    @Override
    @Transactional
    public List<Map<String, Object>> calculateTotalDailyPowerConsumption(LocalDate date) {
        List<Room> rooms = roomRepository.findAll();

        return rooms.parallelStream()
                .map(room -> {
                    double roomConsumption = calculateRoomDailyPowerConsumption(room.getId(), date);
                    Map<String, Object> consumptionData = new HashMap<>();
                    consumptionData.put("roomId", room.getId());
                    consumptionData.put("roomName", room.getName());
                    consumptionData.put("date", date);
                    consumptionData.put("consumption", roomConsumption);
                    return consumptionData;
                })
                .collect(Collectors.toList());
    }

    // 特定年月的耗電量
    @Override
    @Transactional
    public Map<String, Double> calculateMonthlyPowerConsumption(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        return startOfMonth.datesUntil(endOfMonth.plusDays(1))
                .parallel()
                .collect(Collectors.toMap(
                        date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        date -> calculateTotalDailyPowerConsumption(date).stream()
                                .mapToDouble(entry -> (Double) entry.get("consumption"))
                                .sum(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    // 整年每個月的耗電量
    @Override
    @Transactional
    public Map<String, Double> calculateYearlyPowerConsumption(int year) {
        return IntStream.rangeClosed(1, 12)
                .parallel()
                .boxed()
                .collect(Collectors.toMap(
                        month -> String.format("%d-%02d", year, month),
                        month -> calculateMonthlyPowerConsumption(year, month).values().stream()
                                .mapToDouble(Double::doubleValue)
                                .sum(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    // 特定房間一年每個月用電量
    @Override
    @Transactional
    public Map<String, Double> calculateRoomYearlyPowerConsumption(Long roomId, int year) {
        return IntStream.rangeClosed(1, 12)
                .parallel()
                .boxed()
                .collect(Collectors.toMap(
                        month -> String.format("%d-%02d", year, month),
                        month -> calculateRoomMonthlyPowerConsumption(roomId, year, month).values().stream()
                                .mapToDouble(Double::doubleValue)
                                .sum(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    //特定月份每個房間耗電量
    @Override
    @Transactional
    public List<Map<String, Object>> calculateMonthlyRoomPowerConsumption(int year, int month) {
        List<Map<String, Object>> monthlyRoomConsumption = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<Room> rooms = roomRepository.findAll();

        for (Room room : rooms) {
            double monthlyConsumption = 0;
            for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
                monthlyConsumption += calculateRoomDailyPowerConsumption(room.getId(), date);
            }
            Map<String, Object> consumptionData = new HashMap<>();
            consumptionData.put("roomId", room.getId());
            consumptionData.put("roomName", room.getName());
            consumptionData.put("consumption", monthlyConsumption);
            monthlyRoomConsumption.add(consumptionData);
        }

        return monthlyRoomConsumption;
    }
}
