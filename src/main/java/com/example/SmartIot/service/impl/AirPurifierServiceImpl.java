package com.example.SmartIot.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.SmartIot.constant.ResMsg;
import com.example.SmartIot.entity.AirPurifier;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.AirPurifierRepository;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.service.ifs.AirPurifierService;
import com.example.SmartIot.service.ifs.HistoryService;

import jakarta.transaction.Transactional;

@Service
public class AirPurifierServiceImpl implements AirPurifierService {

    @Autowired
    private AirPurifierRepository airPurifierRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired HistoryService historyService;

    @Override
    public List<AirPurifier> getAllAirPurifiers() {
        return airPurifierRepository.findAll();
    }

    @Override
    public AirPurifier getAirPurifierById(Long id) {
        return airPurifierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ResponseEntity<?> saveAirPurifier(AirPurifier airPurifier) {
        if (airPurifier == null || airPurifier.getDevice() == null || airPurifier.getDevice().getId() == null) {
            return new ResponseEntity<>(ResMsg.BAD_REQUEST.getDescription(), HttpStatus.BAD_REQUEST);
        }

        Long deviceId = airPurifier.getDevice().getId();
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        // 檢查設備是否為空氣清潔器
        if (!"air_purifier".equalsIgnoreCase(device.getType())) {
            return new ResponseEntity<>("This device is not an air purifier", HttpStatus.BAD_REQUEST);
        }

        // 使用傳入的 AirPurifier 物件中的 Device 狀態
        Boolean newStatus = airPurifier.getDevice().getStatus();
        if (newStatus == null) {
            return new ResponseEntity<>("Device status cannot be null", HttpStatus.BAD_REQUEST);
        }

        // 更新設備狀態
        device.setStatus(newStatus);
        device = deviceRepository.save(device);

        // 檢查 AirPurifier 表中是否已存在此空氣清潔器
        AirPurifier existingAirPurifier = airPurifierRepository.findById(deviceId).orElse(new AirPurifier());

        // 設定或更新 AirPurifier 的屬性
        existingAirPurifier.setAir_quality(airPurifier.getAir_quality());
        existingAirPurifier.setFan_speed(airPurifier.getFan_speed());
        existingAirPurifier.setOperating_time(airPurifier.getOperating_time());
        existingAirPurifier.setDevice(device);

        // 保存空氣清潔器的設定
        AirPurifier savedAirPurifier = airPurifierRepository.save(existingAirPurifier);

        //保存歷史設定
        if (device.isStatusChanged()) {
            History history = new History();
            history.setDeviceId(deviceId);
            history.setEventType("設備開關");
            history.setDetail(Map.of("status", newStatus));
            historyService.createHistory(history);
        }

        // 創建參數調整事件
        if (!Objects.equals(airPurifier.getAir_quality(), existingAirPurifier.getAir_quality())
                || !Objects.equals(airPurifier.getFan_speed(), existingAirPurifier.getFan_speed())
                || airPurifier.getOperating_time() != existingAirPurifier.getOperating_time()) {
            History paramAdjustEvent = new History();
            paramAdjustEvent.setDeviceId(deviceId);
            paramAdjustEvent.setEventType("設備參數調整");
            Map<String, Object> detail = new HashMap<>();
            if (!Objects.equals(airPurifier.getAir_quality(), existingAirPurifier.getAir_quality())) {
                detail.put("air_quality", airPurifier.getAir_quality());
            }
            if (!Objects.equals(airPurifier.getFan_speed(), existingAirPurifier.getFan_speed())) {
                detail.put("target_temp", airPurifier.getFan_speed());
            }
            if (airPurifier.getOperating_time() != existingAirPurifier.getOperating_time()) {
                detail.put("mode", airPurifier.getOperating_time().toString());
            }
            
            paramAdjustEvent.setDetail(detail);
            historyService.createHistory(paramAdjustEvent);
        }

        return new ResponseEntity<>(savedAirPurifier, HttpStatus.OK);
    }

    @Override
    public void deleteAirPurifier(Long id) {
        airPurifierRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchAirPurifier(Long id, Map<String, Object> updates) {
        AirPurifier airPurifier = airPurifierRepository.findById(id).orElse(null);
        if (airPurifier == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        Device device = airPurifier.getDevice();
        // 判段空氣清潔器是否有關聯的設備
        if (device == null) {
            return new ResponseEntity<>("Associated device not found", HttpStatus.NOT_FOUND);
        }

        boolean statusChanged = false;

        Map<String, Object> detail = new HashMap<>();
        detail.put("roomArea", device.getRoom().getArea());
        detail.put("roomName", device.getRoom().getName());
        detail.put("deviceType", device.getType());
        detail.put("deviceName", device.getName());

        // 開關空氣清潔器
        if (updates.containsKey("status")) {
            Object statusValue = updates.get("status");
            boolean newStatus;
            // 判斷傳入的狀態值是否為整數或布林值
            if (statusValue instanceof Integer) {
                newStatus = ((Integer) statusValue) == 1;
            } else if (statusValue instanceof Boolean) {
                newStatus = (Boolean) statusValue;
            } else {
                return new ResponseEntity<>("Invalid status value. Use 0, 1, true, or false", HttpStatus.BAD_REQUEST);
            }
            device.setStatus(newStatus);
            statusChanged = true;
        }

        // 調整風速
        if (updates.containsKey("fan_speed")) {
            int fanSpeed = (int) updates.get("fan_speed");
            if (fanSpeed < 0 || fanSpeed > 100) {
                return new ResponseEntity<>("Fan speed must be between 0 and 100", HttpStatus.BAD_REQUEST);
            }
            airPurifier.setFan_speed(fanSpeed);
        }

        // 更新空氣品質
        if (updates.containsKey("air_quality")) {
            int airQuality = (int) updates.get("air_quality");
            if (airQuality < 0 || airQuality > 500) {
                return new ResponseEntity<>("Air quality must be between 0 and 500", HttpStatus.BAD_REQUEST);
            }
            airPurifier.setAir_quality(airQuality);
        }

        // 更新運行時間
        if (updates.containsKey("operating_time")) {
            Double operatingTime = (Double) updates.get("operating_time");
            // 運行時間不能為負數
            if (operatingTime < 0) {
                return new ResponseEntity<>("Operating time cannot be negative", HttpStatus.BAD_REQUEST);
            }
            airPurifier.setOperating_time(operatingTime);
        }

        // 如果狀態有變化，保存 Device
        if (statusChanged) {
            deviceRepository.save(device);
        }

        AirPurifier savedAirPurifier = airPurifierRepository.save(airPurifier);

        // 記錄歷史紀錄
        Map<String, Object> changes = new HashMap<>(detail);
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            if (!entry.getKey().equals("status") || statusChanged) {
                changes.put(entry.getKey(), entry.getValue());
            }
        }
        if (!changes.isEmpty()) {
            History history = new History();
            history.setDeviceId(id);
            history.setEventType("設備參數調整");
            history.setDetail(changes);
            historyService.createHistory(history);
        }
        

        return new ResponseEntity<>(savedAirPurifier, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> batchPatchAirPurifiers(List<Map<String, Object>> updates) {
        List<AirPurifier> updatedAirPurifiers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            if (!update.containsKey("id")) {
                errors.add("每個更新項目必須包含 'id' 欄位");
                continue;
            }

            Long id = Long.valueOf(update.get("id").toString());
            Map<String, Object> deviceUpdates = new HashMap<>(update);
            deviceUpdates.remove("id");

            ResponseEntity<?> response = patchAirPurifier(id, deviceUpdates);

            if (response.getStatusCode() == HttpStatus.OK) {
                updatedAirPurifiers.add((AirPurifier) response.getBody());
            } else {
                errors.add("更新設備 ID " + id + " 失敗: " + response.getBody());
            }
        }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.MULTI_STATUS);
        }

        return new ResponseEntity<>(updatedAirPurifiers, HttpStatus.OK);
    }
}