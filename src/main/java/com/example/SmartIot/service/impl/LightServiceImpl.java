package com.example.SmartIot.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.SmartIot.constant.ResMsg;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Light;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.repository.LightRepository;
import com.example.SmartIot.service.ifs.HistoryService;
import com.example.SmartIot.service.ifs.LightService;

import jakarta.transaction.Transactional;

@Service
public class LightServiceImpl implements LightService {

    @Autowired
    private LightRepository lightRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private HistoryService historyService;

    @Override
    public List<Light> getAllLights() {
        return lightRepository.findAll();
    }

    @Override
    public Light getLightById(Long id) {
        return lightRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public ResponseEntity<?> saveLight(Light light) {
        if (light == null || light.getDevice() == null || light.getDevice().getId() == null) {
            return new ResponseEntity<>(ResMsg.BAD_REQUEST.getDescription(), HttpStatus.BAD_REQUEST);
        }

        Long deviceId = light.getDevice().getId();

        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        // 檢查設備是否為燈
        if (!"light".equalsIgnoreCase(device.getType())) {
            return new ResponseEntity<>("This device is not a light", HttpStatus.BAD_REQUEST);
        }

        // 使用傳入的 Light 對象中的 Device 狀態
        Boolean newStatus = light.getDevice().getStatus();
        if (newStatus == null) {
            return new ResponseEntity<>("Device status cannot be null", HttpStatus.BAD_REQUEST);
        }

        // 更新設備狀態
        device.setStatus(newStatus);
        device = deviceRepository.save(device);

        // 創建歷史紀錄 - 開關燈事件
        if (device.isStatusChanged()) {
            History history = new History();
            history.setDeviceId(deviceId);
            history.setEventType("設備開關");
            history.setDetail(Map.of("status", newStatus));
            historyService.createHistory(history);
        }

        // 如果燈是關閉的，將亮度設為0
        if (!newStatus) {
            light.setBrightness(0);
        }

        // 檢查 Light 表中是否已存在此燈
        Light existingLight = lightRepository.findById(deviceId).orElse(new Light());

        // 設置或更新 Light 的屬性
        existingLight.setDevice(device);

        // 僅在亮度或色溫有更改時，創建參數調整事件
        if (light.getBrightness() != existingLight.getBrightness()
                || light.getColor_temp() != existingLight.getColor_temp()) {
            History paramAdjustEvent = new History();
            paramAdjustEvent.setDeviceId(deviceId);
            paramAdjustEvent.setEventType("設備參數調整");
            Map<String, Object> detail = new HashMap<>();
            if (light.getBrightness() != existingLight.getBrightness()) {
                detail.put("brightness", light.getBrightness());
            }
            if (light.getColor_temp() != existingLight.getColor_temp()) {
                detail.put("color_temp", light.getColor_temp());
            }
            paramAdjustEvent.setDetail(detail);
            historyService.createHistory(paramAdjustEvent);
        }

        existingLight.setBrightness(light.getBrightness());
        existingLight.setColor_temp(light.getColor_temp());

        // 保存燈的設置
        Light savedLight = lightRepository.save(existingLight);

        return new ResponseEntity<>(savedLight, HttpStatus.OK);
    }

    @Override
    public void deleteLight(Long id) {
        lightRepository.deleteById(id);
    }

    @Override
    public void deleteLights(List<Long> ids) {
        lightRepository.deleteAllByIds(ids);
    }


    @Override
    @Transactional
    public ResponseEntity<?> patchLight(Long id, Map<String, Object> updates) {
        Light light = lightRepository.findById(id).orElse(null);
        if (light == null) {
            return new ResponseEntity<>(ResMsg.NOT_FOUND.getDescription(), HttpStatus.NOT_FOUND);
        }

        // 取得燈的設備
        Device device = light.getDevice();
        if (device == null) {
            return new ResponseEntity<>("Associated device not found", HttpStatus.NOT_FOUND);
        }

        boolean statusChanged = false;

        Map<String, Object> detail = new HashMap<>();
        detail.put("roomArea", device.getRoom().getArea());
        detail.put("roomName", device.getRoom().getName());
        detail.put("deviceType", device.getType());
        detail.put("deviceName", device.getName());

        // 開關燈
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

            // 如果關閉燈，將亮度設為0
            if (!newStatus) {
                light.setBrightness(0);
            }
        }

        // 調整亮度
        if (updates.containsKey("brightness")) {
            int brightness = (int) updates.get("brightness");
            if (brightness < 0 || brightness > 100) {
                return new ResponseEntity<>("Brightness must be between 0 and 100", HttpStatus.BAD_REQUEST);
            }
            light.setBrightness(brightness);

            // 如果調整亮度且燈原本是關閉的，則打開燈
            // if (brightness > 0 && !device.getStatus()) {
            //     device.setStatus(true);
            //     statusChanged = true;
            // }
        }

        // 調整色調
        if (updates.containsKey("color_temp")) {
            int colorTemp = (int) updates.get("color_temp");
            if (colorTemp < 1000 || colorTemp > 10000) {
                return new ResponseEntity<>("Color temperature must be between 1000K and 10000K",
                        HttpStatus.BAD_REQUEST);
            }
            light.setColor_temp(colorTemp);
        }

        // 如果狀態有變化，保存 Device
        if (statusChanged) {
            deviceRepository.save(device);
        }

        Light savedLight = lightRepository.save(light);

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

        return new ResponseEntity<>(savedLight, HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<?> batchPatchLights(List<Map<String, Object>> updates) {
        List<Light> updatedLights = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> update : updates) {
            if (!update.containsKey("id")) {
                errors.add("每個更新項目必須包含 'id' 欄位");
                continue;
            }

            Long id = Long.valueOf(update.get("id").toString());
            Map<String, Object> lightUpdates = new HashMap<>(update);
            lightUpdates.remove("id");

            ResponseEntity<?> response = patchLight(id, lightUpdates);

            if (response.getStatusCode() == HttpStatus.OK) {
                updatedLights.add((Light) response.getBody());
            } else {
                errors.add("更新設備 ID " + id + " 失敗: " + response.getBody());
            }
        }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.MULTI_STATUS);
        }

        return new ResponseEntity<>(updatedLights, HttpStatus.OK);
    }
}
