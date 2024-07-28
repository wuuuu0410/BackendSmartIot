package com.example.SmartIot.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import com.example.SmartIot.service.ifs.PowerService;

@CrossOrigin
@RestController
@RequestMapping("/power")
public class PowerController {

    @Autowired
    private PowerService powerService;

    //改成以小時

    // 特定設備,特定日期消耗電量
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<?> getDevicePowerConsumption(@PathVariable Long deviceId, @RequestParam LocalDate date) {
        double consumption = powerService.calculateDevicePowerConsumption(deviceId, date);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    // 特定房間,特定日期消耗電量
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomDailyConsumption(@PathVariable Long roomId, @RequestParam LocalDate date) {
        double consumption = powerService.calculateRoomDailyPowerConsumption(roomId, date);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    // 特定房間特定月份的每日耗電量
    @GetMapping("/room/{roomId}/monthly")
    public ResponseEntity<?> getRoomMonthlyConsumption(
            @PathVariable Long roomId,
            @RequestParam int year,
            @RequestParam int month) {
        Map<String, Double> consumption = powerService.calculateRoomMonthlyPowerConsumption(roomId, year, month);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    // 特定日期，總設備消耗電量
    // @GetMapping("/total")
    // public ResponseEntity<?> getTotalDailyConsumption(@RequestParam LocalDate date) {
    //     List<Map<String, Object>> consumption = powerService.calculateTotalDailyPowerConsumption(date);
    //     return new ResponseEntity<>(consumption, HttpStatus.OK);
    // }

    // 該月的每日設備消耗電量
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyConsumption(@RequestParam int year, @RequestParam int month) {
        Map<String, Double> consumption = powerService.calculateMonthlyPowerConsumption(year, month);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    // 特定年份每個月的耗電量
    @GetMapping("/yearly")
    public ResponseEntity<?> getYearlyConsumption(@RequestParam int year) {
        Map<String, Double> consumption = powerService.calculateYearlyPowerConsumption(year);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    // 特定房間一年每月用電量
    @GetMapping("/room/{roomId}/yearly")
    public ResponseEntity<?> getRoomYearlyConsumption(
            @PathVariable Long roomId,
            @RequestParam int year) {
        Map<String, Double> consumption = powerService.calculateRoomYearlyPowerConsumption(roomId, year);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }

    //特定月份每個房間耗電量
    @GetMapping("/total")
    public ResponseEntity<?> getMonthlyRoomConsumption(
            @RequestParam int year,
            @RequestParam int month) {
        List<Map<String, Object>> consumption = powerService.calculateMonthlyRoomPowerConsumption(year, month);
        return new ResponseEntity<>(consumption, HttpStatus.OK);
    }
}
