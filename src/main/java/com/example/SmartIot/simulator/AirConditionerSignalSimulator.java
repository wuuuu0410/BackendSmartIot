package com.example.SmartIot.simulator;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.SmartIot.constant.AirConditionerConstants;
import com.example.SmartIot.entity.AirConditioner;
import com.example.SmartIot.repository.AirConditionerRepository;

@Service
public class AirConditionerSignalSimulator {

    @Autowired
    private AirConditionerRepository airConditionerRepository;
    private Random random = new Random();

    private static final double MAX_TEMP_CHANGE = 0.5; // 每次最大溫度變化
    private static final double INERTIA_FACTOR = 0.9; // 慣性因子，越高越不容易改變

    @Scheduled(fixedRate = 5000) // 每5秒更新一次
    public void simulateSignals() {
        List<AirConditioner> airConditioners = airConditionerRepository.findAll();
        for (AirConditioner ac : airConditioners) {
            if (ac.getDevice().getStatus()) { // 如果空調機是開啟的
                // 更新溫度
                updateTemperature(ac);
            } else {
                // 如果設備關閉，溫度慢慢增加到35度
                increaseTemperature(ac);
            }
            // 模拟環境溫度的微小變化
            simulateEnvironmentTemperature(ac);
            // 更新空調機的狀態
            airConditionerRepository.save(ac);
        }
    }

    private void updateTemperature(AirConditioner ac) {
        double currentTemp = ac.getCurrent_temp();
        double targetTemp = ac.getTarget_temp();
        AirConditionerConstants.Mode mode = ac.getMode();
        AirConditionerConstants.FanSpeed fanSpeed = ac.getFanSpeed();

        // 如果 fanSpeed 為 null，設置一個默認值
        if (fanSpeed == null) {
            fanSpeed = AirConditionerConstants.FanSpeed.MEDIUM; // 或其他適合的默認值
        }

        // 根據模式和風速調整溫度
        double tempChange = calculateTempChange(currentTemp, targetTemp, mode, fanSpeed);

        // 慣性因子和最大變化限制
        tempChange = tempChange * INERTIA_FACTOR;
        tempChange = Math.max(-MAX_TEMP_CHANGE, Math.min(MAX_TEMP_CHANGE, tempChange));

        ac.setCurrent_temp(currentTemp + tempChange);
    }

    private double calculateTempChange(double currentTemp, double targetTemp,
            AirConditionerConstants.Mode mode,
            AirConditionerConstants.FanSpeed fanSpeed) {
        double baseChange = 0.05; // 基礎溫度變化率
        double fanSpeedMultiplier = getFanSpeedMultiplier(fanSpeed);
        double tempDifference = targetTemp - currentTemp;

        switch (mode) {
            case COOL:
                return Math.max(-baseChange * fanSpeedMultiplier, tempDifference * 0.1);
            case HEAT:
                return Math.min(baseChange * fanSpeedMultiplier, tempDifference * 0.1);
            case AUTO:
                return tempDifference * 0.05; // 自動模式緩慢調整
            case FAN:
            default:
                return 0; // 風扇模式不改變溫度
        }
    }

    private double getFanSpeedMultiplier(AirConditionerConstants.FanSpeed fanSpeed) {
        switch (fanSpeed) {
            case LOW:
                return 0.7;
            case MEDIUM:
                return 1.0;
            case HIGH:
                return 1.3;
            case AUTO:
            default:
                return 1.0; // 自動風速假設為中等
        }
    }

    private void simulateEnvironmentTemperature(AirConditioner ac) {
        // 模擬環境溫度的微小變化
        double environmentalChange = (random.nextDouble() - 0.5) * 0.1; // -0.05 到 0.05 之間的隨機值
        double newTemp = ac.getCurrent_temp() + environmentalChange;
        // 確保溫度在合理範圍內，例如 10 到 40 度
        ac.setCurrent_temp(Math.max(10, Math.min(40, newTemp)));
    }

    private void increaseTemperature(AirConditioner ac) {
        double currentTemp = ac.getCurrent_temp();
        double targetTemp = 35.0;

        if (currentTemp < targetTemp) {
            // 計算溫度差
            double tempDifference = targetTemp - currentTemp;
            // 基礎升溫速率
            double baseIncrease = 0.01 + random.nextDouble() * 0.02;
            // 應用慣性因子
            double increase = baseIncrease * INERTIA_FACTOR + tempDifference * 0.01;
            // 限制最大變化
            increase = Math.min(increase, MAX_TEMP_CHANGE);

            double newTemp = Math.min(targetTemp, currentTemp + increase);
            ac.setCurrent_temp(newTemp);
        }
    }
}