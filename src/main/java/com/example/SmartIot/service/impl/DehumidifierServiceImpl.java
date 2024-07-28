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

import com.example.SmartIot.constant.AirConditionerConstants;
import com.example.SmartIot.constant.ResMsg;
import com.example.SmartIot.entity.Dehumidifier;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.DehumidifierRepository;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.service.ifs.DehumidifierService;
import com.example.SmartIot.service.ifs.HistoryService;

import jakarta.transaction.Transactional;

@Service
public class DehumidifierServiceImpl implements DehumidifierService {

    @Autowired
    private DehumidifierRepository dehumidifierRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired HistoryService historyService;

    @Override
    public List<Dehumidifier> getAllDehumidifiers() {
        return dehumidifierRepository.findAll();
    }

    @Override
    public Dehumidifier getDehumidifierById(Long id) {
        return dehumidifierRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ResponseEntity<?> saveDehumidifier(Dehumidifier dehumidifier) {
        if (dehumidifier == null || dehumidifier.getDevice() == null || dehumidifier.getDevice().getId() == null) {
            return new ResponseEntity<>(ResMsg.BAD_REQUEST.getDescription(), HttpStatus.BAD_REQUEST);
        }

        Long deviceId = dehumidifier.getDevice().getId();
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        // 檢查設備是否為除濕機
        if (!"dehumidifier".equalsIgnoreCase(device.getType())) {
            return new ResponseEntity<>("This device is not a dehumidifier", HttpStatus.BAD_REQUEST);
        }

        // 使用傳入的 Dehumidifier 物件中的 Device 狀態
        Boolean newStatus = dehumidifier.getDevice().getStatus();
        if (newStatus == null) {
            return new ResponseEntity<>("Device status cannot be null", HttpStatus.BAD_REQUEST);
        }

        // 更新設備狀態
        device.setStatus(newStatus);
        device = deviceRepository.save(device);

        // 檢查 Dehumidifier 表中是否已存在此除濕機
        Dehumidifier existingDehumidifier = dehumidifierRepository.findById(deviceId).orElse(new Dehumidifier());

        // 設定或更新 Dehumidifier 的屬性
        existingDehumidifier.setCurrent_humidity(dehumidifier.getCurrent_humidity());
        existingDehumidifier.setTarget_humidity(dehumidifier.getTarget_humidity());
        existingDehumidifier.setTank_capacity(dehumidifier.getTank_capacity());
        existingDehumidifier.setFanSpeed(dehumidifier.getFanSpeed());
        existingDehumidifier.setDevice(device);

        // 保存除濕機的設定
        Dehumidifier savedDehumidifier = dehumidifierRepository.save(existingDehumidifier);
        
        // 創建歷史紀錄 - 開關空調機事件
        if (device.isStatusChanged()) {
            History history = new History();
            history.setDeviceId(deviceId);
            history.setEventType("設備開關");
            history.setDetail(Map.of("status", newStatus));
            historyService.createHistory(history);
        }

        // 僅在溫度、模式或風速有更改時，創建參數調整事件
        if (!Objects.equals(dehumidifier.getCurrent_humidity(), existingDehumidifier.getCurrent_humidity())
                || !Objects.equals(dehumidifier.getTarget_humidity(), existingDehumidifier.getTarget_humidity())
                || dehumidifier.getTank_capacity() != existingDehumidifier.getTank_capacity()
                || dehumidifier.getFanSpeed() != existingDehumidifier.getFanSpeed()) {
            History paramAdjustEvent = new History();
            paramAdjustEvent.setDeviceId(deviceId);
            paramAdjustEvent.setEventType("設備參數調整");
            Map<String, Object> detail = new HashMap<>();
            if (!Objects.equals(dehumidifier.getCurrent_humidity(), existingDehumidifier.getCurrent_humidity())) {
                detail.put("current_temp", dehumidifier.getCurrent_humidity());
            }
            if (!Objects.equals(dehumidifier.getTarget_humidity(), existingDehumidifier.getTarget_humidity())) {
                detail.put("target_temp", dehumidifier.getTarget_humidity());
            }
            if (dehumidifier.getTank_capacity() != existingDehumidifier.getTank_capacity()) {
                detail.put("mode", dehumidifier.getTank_capacity().toString());
            }
            if (dehumidifier.getFanSpeed() != existingDehumidifier.getFanSpeed()) {
                detail.put("fan_speed", dehumidifier.getFanSpeed().toString());
            }
            paramAdjustEvent.setDetail(detail);
            historyService.createHistory(paramAdjustEvent);
        }

        return new ResponseEntity<>(savedDehumidifier, HttpStatus.OK);
    }

    @Override
    public void deleteDehumidifier(Long id) {
        dehumidifierRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchDehumidifier(Long id, Map<String, Object> updates) {
        Dehumidifier dehumidifier = dehumidifierRepository.findById(id).orElse(null);
        if (dehumidifier == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        Device device = dehumidifier.getDevice();
        if (device == null) {
            return new ResponseEntity<>("Associated device not found", HttpStatus.NOT_FOUND);
        }

        boolean statusChanged = false;

        Map<String, Object> detail = new HashMap<>();
        detail.put("roomArea", device.getRoom().getArea());
        detail.put("roomName", device.getRoom().getName());
        detail.put("deviceType", device.getType());
        detail.put("deviceName", device.getName());

        // 開關除濕機
        if (updates.containsKey("status")) {
            Object statusValue = updates.get("status");
            boolean newStatus;
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

        // 更新當前濕度
        if (updates.containsKey("current_humidity")) {
            Double currentHumidity = (Double) updates.get("current_humidity");
            if (currentHumidity < 0 || currentHumidity > 100) {
                return new ResponseEntity<>("Current humidity must be between 0 and 100", HttpStatus.BAD_REQUEST);
            }
            dehumidifier.setCurrent_humidity(currentHumidity);
        }

        // 更新目標濕度
        if (updates.containsKey("target_humidity")) {
            Object targetHumidityObj = updates.get("target_humidity");
            Double targetHumidity;

            if (targetHumidityObj instanceof Integer) {
                targetHumidity = ((Integer) targetHumidityObj).doubleValue();
            } else if (targetHumidityObj instanceof Double) {
                targetHumidity = (Double) targetHumidityObj;
            } else {
                return new ResponseEntity<>("Invalid target humidity format", HttpStatus.BAD_REQUEST);
            }

            if (targetHumidity < 0 || targetHumidity > 100) {
                return new ResponseEntity<>("Target humidity must be between 0 and 100", HttpStatus.BAD_REQUEST);
            }

            dehumidifier.setTarget_humidity(targetHumidity);
        }

        // 更新水箱容量
        if (updates.containsKey("tank_capacity")) {
            Double tankCapacity = (Double) updates.get("tank_capacity");
            if (tankCapacity < 0) {
                return new ResponseEntity<>("Tank capacity cannot be negative", HttpStatus.BAD_REQUEST);
            }
            dehumidifier.setTank_capacity(tankCapacity);
        }

        // 更新風速
        if (updates.containsKey("fan_speed")) {
            try {
                dehumidifier.setFanSpeed(AirConditionerConstants.FanSpeed.valueOf((String) updates.get("fan_speed")));
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>("Invalid fan_speed value", HttpStatus.BAD_REQUEST);
            }
        }

        // 如果狀態有變化，保存 Device
        if (statusChanged) {
            deviceRepository.save(device);
        }

        Dehumidifier savedDehumidifier = dehumidifierRepository.save(dehumidifier);

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
        

        return new ResponseEntity<>(savedDehumidifier, HttpStatus.OK);
    }

    // 批次更新除濕機
    @Override
    @Transactional
    public ResponseEntity<?> batchPatchDehumidifiers(List<Map<String, Object>> updates) {
        List<Dehumidifier> updatedDehumidifiers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            if (!update.containsKey("id")) {
                errors.add("每個更新項目必須包含 'id' 欄位");
                continue;
            }

            Long id = Long.valueOf(update.get("id").toString());
            Map<String, Object> dehumidifierUpdates = new HashMap<>(update);
            dehumidifierUpdates.remove("id");

            ResponseEntity<?> response = patchDehumidifier(id, dehumidifierUpdates);

            if (response.getStatusCode() == HttpStatus.OK) {
                updatedDehumidifiers.add((Dehumidifier) response.getBody());
            } else {
                errors.add("更新設備 ID " + id + " 失敗: " + response.getBody());
            }
        }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.MULTI_STATUS);
        }

        return new ResponseEntity<>(updatedDehumidifiers, HttpStatus.OK);
    }
}
