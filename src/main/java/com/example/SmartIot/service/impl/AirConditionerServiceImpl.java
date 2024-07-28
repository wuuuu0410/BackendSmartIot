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
import com.example.SmartIot.entity.AirConditioner;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.AirConditionerRepository;
import com.example.SmartIot.repository.DeviceRepository;    
import com.example.SmartIot.service.ifs.AirConditionerService;
import com.example.SmartIot.service.ifs.HistoryService;

import jakarta.transaction.Transactional;

@Service
public class AirConditionerServiceImpl implements AirConditionerService {

    @Autowired
    private AirConditionerRepository airConditionerRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private HistoryService historyService;

    @Override
    public List<AirConditioner> getAllAirConditioners() {
        return airConditionerRepository.findAll();
    }

    @Override
    public AirConditioner getAirConditionerById(Long id) {
        return airConditionerRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ResponseEntity<?> saveAirConditioner(AirConditioner airConditioner) {
        if (airConditioner == null || airConditioner.getDevice() == null
                || airConditioner.getDevice().getId() == null) {
            return new ResponseEntity<>(ResMsg.BAD_REQUEST.getDescription(), HttpStatus.BAD_REQUEST);
        }

        Long deviceId = airConditioner.getDevice().getId();
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        // 檢查設備是否為空調機
        if (!"ac".equalsIgnoreCase(device.getType())) {
            return new ResponseEntity<>("This device is not an air conditioner", HttpStatus.BAD_REQUEST);
        }

        // 使用傳入的 AirConditioner 物件中的 Device 狀態
        Boolean newStatus = airConditioner.getDevice().getStatus();
        if (newStatus == null) {
            return new ResponseEntity<>("Device status cannot be null", HttpStatus.BAD_REQUEST);
        }

        // 更新設備狀態
        device.setStatus(newStatus);
        device = deviceRepository.save(device);

        // 檢查 AirConditioner 表中是否已存在此空調機
        AirConditioner existingAirConditioner = airConditionerRepository.findById(deviceId)
                .orElse(new AirConditioner());

        // 設定或更新 AirConditioner 的屬性
        existingAirConditioner.setCurrent_temp(airConditioner.getCurrent_temp());
        existingAirConditioner.setTarget_temp(airConditioner.getTarget_temp());
        existingAirConditioner.setMode(airConditioner.getMode());
        existingAirConditioner.setFanSpeed(airConditioner.getFanSpeed());
        existingAirConditioner.setDevice(device);

        // 保存空調機的設定
        AirConditioner savedAirConditioner = airConditionerRepository.save(existingAirConditioner);

        // 保存詳細訊息
        Map<String, Object> detail = new HashMap<>();
        detail.put("status", newStatus);
        detail.put("roomArea", device.getRoom().getArea());
        detail.put("roomName", device.getRoom().getName());
        detail.put("deviceType", device.getType());
        detail.put("deviceName", device.getName());

        // 創建歷史紀錄 - 開關空調機事件
        if (device.isStatusChanged()) {
            History history = new History();
            history.setDeviceId(deviceId);
            history.setEventType("設備開關");
            history.setDetail(detail);
            historyService.createHistory(history);
        }

        // 僅在溫度、模式或風速有更改時，創建參數調整事件
        if (!Objects.equals(airConditioner.getCurrent_temp(), existingAirConditioner.getCurrent_temp())
                || !Objects.equals(airConditioner.getTarget_temp(), existingAirConditioner.getTarget_temp())
                || airConditioner.getMode() != existingAirConditioner.getMode()
                || airConditioner.getFanSpeed() != existingAirConditioner.getFanSpeed()) {
            History paramAdjustEvent = new History();
            paramAdjustEvent.setDeviceId(deviceId);
            paramAdjustEvent.setEventType("設備參數調整");
            Map<String, Object> paramDetail = new HashMap<>();
            if (!Objects.equals(airConditioner.getCurrent_temp(), existingAirConditioner.getCurrent_temp())) {
                paramDetail.put("current_temp", airConditioner.getCurrent_temp());
            }
            if (!Objects.equals(airConditioner.getTarget_temp(), existingAirConditioner.getTarget_temp())) {
                paramDetail.put("target_temp", airConditioner.getTarget_temp());
            }
            if (airConditioner.getMode() != existingAirConditioner.getMode()) {
                paramDetail.put("mode", airConditioner.getMode().toString());
            }
            if (airConditioner.getFanSpeed() != existingAirConditioner.getFanSpeed()) {
                paramDetail.put("fan_speed", airConditioner.getFanSpeed().toString());
            }
            paramAdjustEvent.setDetail(paramDetail);
            historyService.createHistory(paramAdjustEvent);
        }

        return new ResponseEntity<>(savedAirConditioner, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> patchAirConditioner(Long id, Map<String, Object> updates) {
        AirConditioner airConditioner = airConditionerRepository.findById(id).orElse(null);
        if (airConditioner == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        Device device = airConditioner.getDevice();
        if (device == null) {
            return new ResponseEntity<>("Associated device not found", HttpStatus.NOT_FOUND);
        }

        boolean statusChanged = false;

        Map<String, Object> detail = new HashMap<>();
        detail.put("roomArea", device.getRoom().getArea());
        detail.put("roomName", device.getRoom().getName());
        detail.put("deviceType", device.getType());
        detail.put("deviceName", device.getName());

        // 開關空調機
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
            detail.put("status", newStatus);
            statusChanged = true;
        }

        // 更新當前溫度
        if (updates.containsKey("current_temp")) {
            airConditioner.setCurrent_temp((Double) updates.get("current_temp"));
        }

        // 更新目標溫度；
        if (updates.containsKey("target_temp")) {
            Object targetTempObj = updates.get("target_temp");
            Double targetTemp;

            if (targetTempObj instanceof Integer) {
                targetTemp = ((Integer) targetTempObj).doubleValue();
            } else if (targetTempObj instanceof Double) {
                targetTemp = (Double) targetTempObj;
            } else {
                return new ResponseEntity<>("Invalid target temperature format", HttpStatus.BAD_REQUEST);
            }

            // 可以根據需要添加溫度範圍檢查
            if (targetTemp < 16 || targetTemp > 30) {
                return new ResponseEntity<>("Target temperature must be between 16 and 30", HttpStatus.BAD_REQUEST);
            }

            airConditioner.setTarget_temp(targetTemp);
        }

        // 更新模式
        if (updates.containsKey("mode")) {
            try {
                airConditioner.setMode(AirConditionerConstants.Mode.valueOf((String) updates.get("mode")));
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>("Invalid mode value", HttpStatus.BAD_REQUEST);
            }
        }

        // 更新風速
        if (updates.containsKey("fan_speed")) {
            try {
                airConditioner.setFanSpeed(AirConditionerConstants.FanSpeed.valueOf((String) updates.get("fan_speed")));
            } catch (IllegalArgumentException e) {
                return new ResponseEntity<>("Invalid fan_speed value", HttpStatus.BAD_REQUEST);
            }
        }

        // 如果狀態有變化，保存 Device
        if (statusChanged) {
            deviceRepository.save(device);
        }

        AirConditioner savedAirConditioner = airConditionerRepository.save(airConditioner);

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

        return new ResponseEntity<>(savedAirConditioner, HttpStatus.OK);
    }

    @Override
    public void deleteAirConditioner(Long id) {
        airConditionerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ResponseEntity<?> batchPatchAirConditioners(List<Map<String, Object>> updates) {
        List<AirConditioner> updatedAirConditioners = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            if (!update.containsKey("id")) {
                errors.add("每個更新項目必須包含 'id' 欄位");
                continue;
            }

            Long id = Long.valueOf(update.get("id").toString());
            Map<String, Object> airConditionerUpdates = new HashMap<>(update);
            airConditionerUpdates.remove("id");

            ResponseEntity<?> response = patchAirConditioner(id, airConditionerUpdates);

            if (response.getStatusCode() == HttpStatus.OK) {
                updatedAirConditioners.add((AirConditioner) response.getBody());
            } else {
                errors.add("更新設備 ID " + id + " 失敗: " + response.getBody());
            }
        }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.MULTI_STATUS);
        }

        return new ResponseEntity<>(updatedAirConditioners, HttpStatus.OK);
    }
}