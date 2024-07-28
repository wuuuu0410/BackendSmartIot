package com.example.SmartIot.simulator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.SmartIot.entity.AirPurifier;
import com.example.SmartIot.repository.AirPurifierRepository;

import java.util.List;
import java.util.Random;

@Service
public class AirPurifierSignalSimulator {

    @Autowired
    private AirPurifierRepository airPurifierRepository;

    private Random random = new Random();

    private static final double AIR_QUALITY_INERTIA_FACTOR = 0.9; // 慣性因子，越高越不容易改變
    private static final int MAX_AIR_QUALITY_CHANGE = 5; // 最大變化
    private static final int BASE_WORST_AIR_QUALITY = 200; // 最差空氣品質
    private static final int WORST_AIR_QUALITY_RANGE = 20; // 最差空氣品質範圍

    @Scheduled(fixedRate = 5000) // 每5秒更新一次
    public void simulateSignals() {
        List<AirPurifier> airPurifiers = airPurifierRepository.findAll();

        for (AirPurifier ap : airPurifiers) {
            if (ap.getDevice().getStatus()) { // 如果空氣清潔器是開啟的
                // 更新空氣品質
                updateAirQuality(ap);
                // 更新運行時間
                updateOperatingTime(ap);
            } else {
                // 如果設備關閉，空氣品質慢慢變差
                deteriorateAirQuality(ap);
            }

            // 模擬環境空氣品質的微小變化
            simulateEnvironmentAirQuality(ap);
            // 更新空氣清潔器的狀態
            airPurifierRepository.save(ap);
        }
    }

    private void updateAirQuality(AirPurifier ap) {
        int currentAirQuality = ap.getAir_quality();
        int fanSpeed = ap.getFan_speed();

        // 根據風速改善空氣品質
        // 假設風速越高，空氣品質改善越快
        int improvement = (int) (fanSpeed * 0.1); // 0-10 的改善
        ap.setAir_quality(Math.max(0, currentAirQuality - improvement));
    }

    private void updateOperatingTime(AirPurifier ap) {
        // 增加運行時間，假設每次更新增加5秒（因為更新間隔是5秒）
        ap.setOperating_time(ap.getOperating_time() + 5.0 / 3600); // 轉換為小時
    }

    private void simulateEnvironmentAirQuality(AirPurifier ap) {
        // 模擬環境空氣品質的微小變化
        int environmentalChange = random.nextInt(11) - 5; // -5 到 5 之間的隨機值
        int newAirQuality = ap.getAir_quality() + environmentalChange;
        // 確保空氣品質在 0-500 的範圍內
        ap.setAir_quality(Math.max(0, Math.min(500, newAirQuality)));
    }

    private void deteriorateAirQuality(AirPurifier ap) {
        int currentAirQuality = ap.getAir_quality();
        int targetAirQuality = BASE_WORST_AIR_QUALITY + random.nextInt(WORST_AIR_QUALITY_RANGE)
                - WORST_AIR_QUALITY_RANGE / 2;

        if (currentAirQuality < targetAirQuality) {
            // 計算空氣品質差異
            int qualityDifference = targetAirQuality - currentAirQuality;
            // 基礎惡化速率
            double baseDeteriorationRate = 1 + random.nextDouble() * 2;
            // 應用慣性因子
            double deterioration = baseDeteriorationRate * AIR_QUALITY_INERTIA_FACTOR + qualityDifference * 0.05;
            // 限制最大變化
            deterioration = Math.min(deterioration, MAX_AIR_QUALITY_CHANGE);

            int newAirQuality = Math.min(targetAirQuality, (int) (currentAirQuality + deterioration));
            ap.setAir_quality(newAirQuality);
        }
    }
}
