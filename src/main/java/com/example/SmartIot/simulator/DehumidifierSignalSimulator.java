package com.example.SmartIot.simulator;

import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.SmartIot.entity.Dehumidifier;
import com.example.SmartIot.repository.DehumidifierRepository;

@Service
public class DehumidifierSignalSimulator {

    @Autowired
    private DehumidifierRepository dehumidifierRepository;
    private Random random = new Random();
    private static final double HUMIDITY_INERTIA_FACTOR = 0.9; // 慣性因子，越高越不容易改變
    private static final double MAX_HUMIDITY_CHANGE = 0.5; // 最大變化
    private static final double DEFAULT_HUMIDITY = 80.0; // 預設濕度
    private static final double MAX_HUMIDITY = 90.0; // 最大濕度

    @Scheduled(fixedRate = 5000) // 每5秒更新一次
    public void simulateSignals() {
        List<Dehumidifier> dehumidifiers = dehumidifierRepository.findAll();
        for (Dehumidifier dh : dehumidifiers) {
            if (dh.getCurrent_humidity() == 0) { // 如果當前濕度為0，設置為預設值
                dh.setCurrent_humidity(DEFAULT_HUMIDITY);
            }
            if (dh.getDevice().getStatus()) { // 如果除濕機是開啟的
                // 更新當前濕度
                updateCurrentHumidity(dh);
                // 更新水箱容量
                updateTankCapacity(dh);
            } else {
                // 如果設備關閉，濕度慢慢增加到90
                increaseHumidity(dh);
            }
            // 模擬環境濕度的微小變化
            simulateEnvironmentHumidity(dh);
            // 更新除濕機的狀態
            dehumidifierRepository.save(dh);
        }
    }

    private void updateCurrentHumidity(Dehumidifier dh) {
        double currentHumidity = dh.getCurrent_humidity();
        double targetHumidity = dh.getTarget_humidity();

        // 計算濕度差
        double humidityDifference = targetHumidity - currentHumidity;
        // 基礎降低速率
        double baseReduction = 0.1 + random.nextDouble() * 0.4;
        // 應用慣性因子
        double reduction = baseReduction * HUMIDITY_INERTIA_FACTOR + Math.abs(humidityDifference) * 0.01;
        // 限制最大變化
        reduction = Math.min(reduction, MAX_HUMIDITY_CHANGE);

        if (currentHumidity > targetHumidity) {
            dh.setCurrent_humidity(Math.max(targetHumidity, currentHumidity - reduction));
        }
    }

    private void updateTankCapacity(Dehumidifier dh) {
        // 假設每5秒，水箱容量增加0.01-0.05升
        double increase = 0.01 + random.nextDouble() * 0.04;
        double newCapacity = dh.getTank_capacity() + increase;
        // 假設最大水箱容量為5升
        dh.setTank_capacity(Math.min(5.0, newCapacity));
    }

    private void simulateEnvironmentHumidity(Dehumidifier dh) {
        // 模擬環境濕度的微小變化
        double environmentalChange = (random.nextDouble() * 2 - 1) * 0.5; // -0.5 到 0.5 之間的隨機值
        double newHumidity = dh.getCurrent_humidity() + environmentalChange;
        // 確保濕度在 0-100 的範圍內
        dh.setCurrent_humidity(Math.max(0, Math.min(100, newHumidity)));
    }

    private void increaseHumidity(Dehumidifier dh) {
        double currentHumidity = dh.getCurrent_humidity();

        // 計算濕度差
        double humidityDifference = MAX_HUMIDITY - currentHumidity;
        // 基礎增加速率
        double baseIncrease = 0.1 + random.nextDouble() * 0.4;
        // 應用慣性因子
        double increase = baseIncrease * HUMIDITY_INERTIA_FACTOR + humidityDifference * 0.01;
        // 限制最大變化
        increase = Math.min(increase, MAX_HUMIDITY_CHANGE);

        if (currentHumidity < MAX_HUMIDITY) {
            double newHumidity = Math.min(MAX_HUMIDITY, currentHumidity + increase);
            dh.setCurrent_humidity(newHumidity);
        }
    }
}