package com.example.SmartIot.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.SmartIot.entity.AirConditioner;
import com.example.SmartIot.entity.AirPurifier;
import com.example.SmartIot.entity.Dehumidifier;
import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Light;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.AirConditionerRepository;
import com.example.SmartIot.repository.AirPurifierRepository;
import com.example.SmartIot.repository.DehumidifierRepository;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.repository.HistoryRepository;
import com.example.SmartIot.repository.LightRepository;
import com.example.SmartIot.repository.RoomRepository;
import com.example.SmartIot.service.ifs.DeviceService;
import com.example.SmartIot.vo.DeviceReq;

import jakarta.transaction.Transactional;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final RoomRepository roomRepository;
    private final AirPurifierRepository airPurifierRepository;
    private final DehumidifierRepository dehumidifierRepository;
    private final LightRepository lightRepository;
    private final AirConditionerRepository airConditionerRepository;
    private final HistoryRepository historyRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository, RoomRepository roomRepository,
            AirPurifierRepository airPurifierRepository, DehumidifierRepository dehumidifierRepository,
            LightRepository lightRepository, AirConditionerRepository airConditionerRepository,
            HistoryRepository historyRepository) {
        this.deviceRepository = deviceRepository;
        this.roomRepository = roomRepository;
        this.airPurifierRepository = airPurifierRepository;
        this.dehumidifierRepository = dehumidifierRepository;
        this.lightRepository = lightRepository;
        this.airConditionerRepository = airConditionerRepository;
        this.historyRepository = historyRepository;
    }

    // 返回所有設備的列表
    @Override
    public List<Device> getAllDevices() {

        List<Device> devices = deviceRepository.findAll();
        // 按照設備類型分群
        Map<String, List<Device>> groupedDevices = devices.stream()
            .collect(Collectors.groupingBy(Device::getType));

        // 對每個設備類型的群組按照ID排序
        groupedDevices.values().forEach(list -> list.sort(Comparator.comparing(Device::getId)));

        // 按照設備類型排序後的結果
        List<Device> sortedDevices = groupedDevices.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        return sortedDevices;
    }

    // 根據設備 ID 返回相應的設備，如果找不到則拋出異常
    @Override
    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Device not found"));
    }

    // 搜尋設備 名稱及種類
    @Override
    public List<Device> searchDevices(String name, String type, String area, Boolean status) {
        List<Device> devices = deviceRepository.findByCriteria(name, type, area, status);

        // 設置每個設備的房間的區域
        for (Device device : devices) {
            Room room = device.getRoom();
            if (room != null) {
                device.setArea(room.getArea());
                device.setRoomId(room.getId()); // Set the roomId
            }
        }

        return devices;
    }

    // 創建新設備 或 更新設備
    @Override
    public Device saveDevice(DeviceReq deviceReq) {

        Device device;
        boolean isNew = false;

        //如果有 id 就更新設備
        if (deviceReq.getId() != null) {
            device = deviceRepository.findById(deviceReq.getId())
                    .orElseThrow(() -> new RuntimeException("Device not found"));
                if (deviceReq.getName() != null) {
                    device.setName(deviceReq.getName());
                }
                // if (deviceReq.getType() != null) {
                //     device.setType(deviceReq.getType());
                // }
                if (deviceReq.getStatus() != null) {
                    device.setStatus(deviceReq.getStatus());
                } else {
                    device.setStatus(false);
                }
                if (deviceReq.getTime() != null) {
                    device.setTime(deviceReq.getTime());
                }
                if (deviceReq.getRoomId() != null) {
                    Room room = roomRepository.findById(deviceReq.getRoomId())
                            .orElseThrow(() -> new RuntimeException("Room not found with id " + deviceReq.getRoomId()));
                    device.setRoom(room);
                }
                //如果開關狀態有更新就紀錄
                if (device.isStatusChanged()) {
                    Map<String, Object> historyDetail = new HashMap<>();
                    historyDetail.put("deviceName", device.getName());
                    historyDetail.put("deviceType", device.getType());
                    historyDetail.put("status", device.getStatus() ? "開" : "關");
                    if (device.getRoom() != null) {
                        historyDetail.put("roomName", device.getRoom().getName());
                        historyDetail.put("roomArea", device.getRoom().getArea());
                    }
                    saveHistoryRecord(device.getId(), "設備開關", historyDetail);
                    device.setStatusChanged(false);
                }
        } else {
            // 沒 id 就創建新設備
            device = new Device();
            device.setName(deviceReq.getName());
            device.setType(deviceReq.getType());
            device.setStatus(deviceReq.getStatus());
            device.setTime(deviceReq.getTime());
            //是否放在哪個房間
            if(deviceReq.getRoomId() != null) {
                Room room = roomRepository.findById(deviceReq.getRoomId())
                        .orElseThrow(() -> new RuntimeException("Room not found with id " + deviceReq.getRoomId()));
                device.setRoom(room);
            } else {
                // 沒有就 null
                device.setRoom(null);
            }
            // 設置默認的功率消耗率
            setDefaultPowerConsumptionRate(device);

            isNew = true;
        }

        Device savedDevice = deviceRepository.save(device);

        // 根據設備類型在相關表中新增資訊
        switch (device.getType()) {
            case "空氣清淨機":
                AirPurifier airPurifier;
                if (isNew) {
                    airPurifier = new AirPurifier();
                    airPurifier.setDevice(savedDevice);
                    airPurifier.setAir_quality(0);
                    airPurifier.setFan_speed(0);
                    airPurifier.setOperating_time(0.0);
                } else {
                    airPurifier = airPurifierRepository.findByDeviceId(savedDevice.getId());
                }
                airPurifierRepository.save(airPurifier);
                break;

            case "除濕機":
                Dehumidifier dehumidifier;
                if (isNew) {
                    dehumidifier = new Dehumidifier();
                    dehumidifier.setDevice(savedDevice);
                    dehumidifier.setCurrent_humidity(0.0);
                    dehumidifier.setTarget_humidity(0.0);
                    dehumidifier.setTank_capacity(0.0);
                } else {
                    dehumidifier = dehumidifierRepository.findByDeviceId(savedDevice.getId());
                }
                dehumidifierRepository.save(dehumidifier);
                break;

            case "燈":
                Light light;
                if (isNew) {
                    light = new Light();
                    light.setDevice(savedDevice);
                    light.setBrightness(0);
                    light.setColor_temp(0);
                } else {
                    light = lightRepository.findByDeviceId(savedDevice.getId());
                }
                lightRepository.save(light);
                break;

            case "冷氣機":
                AirConditioner airConditioner;
                if (isNew) {
                    airConditioner = new AirConditioner();
                    airConditioner.setDevice(savedDevice);
                    airConditioner.setCurrent_temp(0.0);
                    airConditioner.setTarget_temp(17.0);
                } else {
                    airConditioner = airConditionerRepository.findByDeviceId(savedDevice.getId());
                }
                airConditionerRepository.save(airConditioner);
                break;

            // 可新增其他設備類型

            default:
                throw new RuntimeException("Unsupported device type: " + device.getType());
        }

        if (isNew) {
            // 新增設備的歷史紀錄
            Map<String, Object> detail = new HashMap<>();
            detail.put("deviceName", savedDevice.getName());
            detail.put("deviceType", savedDevice.getType());
            
            // 如果有房間信息，添加到歷史紀錄中
            Room room = savedDevice.getRoom();
            if (room != null) {
                detail.put("roomName", room.getName());
                detail.put("roomArea", room.getArea());
            } else {
                detail.put("roomName", "無使用房間");
                detail.put("roomArea", "暫存空間");
            }
            // 新增設備的歷史紀錄
            saveHistoryRecord(savedDevice.getId(), "新增設備", detail);
        }

        return savedDevice;
    }

    // 刪除指定 ID 的設備
    @Override
    @Transactional
    public ResponseEntity<String> deleteDevice(Long id) {
        try{
            Device device = deviceRepository.findById(id).orElseThrow(() -> new RuntimeException("Device not found"));
            switch (device.getType()) {
                case "空氣清淨機":
                    airPurifierRepository.deleteById(id);
                    break;
                case "除濕機":
                    dehumidifierRepository.deleteById(id);
                    break;
                case "燈":
                    lightRepository.deleteById(id);
                    break;
                case "冷氣機":
                    airConditionerRepository.deleteById(id);
                    break;
                default:
                    throw new RuntimeException("Unsupported device type: " + device.getType());
            }
            deviceRepository.delete(device);
            // 歷史紀錄欄位區
            Map<String, Object> detail = new HashMap<>();
            detail.put("deviceName", device.getName());
            detail.put("deviceType", device.getType());
            
            // 如果有房間信息，添加到歷史紀錄中
            Room room = device.getRoom();
            if (room != null) {
                detail.put("roomName", room.getName());
                detail.put("roomArea", room.getArea());
            } else {
                detail.put("roomName", "無使用房間");
                detail.put("roomArea", "暫存空間");
            }

            // 刪除設備的歷史紀錄
            saveHistoryRecord(id, "刪除設備", detail);
            return ResponseEntity.ok("Device deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete device: " + e.getMessage());
        }
    }

    //刪除多台設備
    @Override
    @Transactional
    public ResponseEntity<String> deleteDevices(List<Long> ids) {
        try{
            // 遍歷所有要刪除的設備 ID
            for (Long id : ids) {
                Device device = deviceRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
                String deviceType = device.getType();

                // 根據設備類型刪除對應的子表記錄
                switch (deviceType) {
                    case "空氣清淨機":
                        airPurifierRepository.deleteById(id);
                        break;
                    case "除濕機":
                        dehumidifierRepository.deleteById(id);
                        break;
                    case "燈":
                        lightRepository.deleteById(id);
                        break;
                    case "冷氣機":
                        airConditionerRepository.deleteById(id);
                        break;
                    default:
                        throw new RuntimeException("Unsupported device type: " + deviceType);
                }
                // 刪除主設備表中的記錄
                deviceRepository.delete(device);
                // 歷史紀錄欄位區
                Map<String, Object> detail = new HashMap<>();
                detail.put("deviceName", device.getName());
                detail.put("deviceType", device.getType()); 
                // 如果有房間信息，添加到歷史紀錄中
                Room room = device.getRoom();
                if (room != null) {
                    detail.put("roomName", room.getName());
                    detail.put("roomArea", room.getArea());
                } else {
                    detail.put("roomName", "無使用房間");
                    detail.put("roomArea", "暫存空間");
                }
                // 刪除設備的歷史紀錄
                saveHistoryRecord(id, "刪除設備", detail);
            }
            return ResponseEntity.ok("Devices deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete devices: " + e.getMessage());
    }
}

        @Override
        @Transactional
        public ResponseEntity<String> saveDevices(Set<Device> devices) {
            try {
                deviceRepository.saveAll(devices);
                return ResponseEntity.ok("Devices saved successfully");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save devices: " + e.getMessage());
            }
        }

        

        private void saveHistoryRecord(Long deviceId, String eventType, Map<String, Object> detail) {
            History history = new History();
            history.setEventId(UUID.randomUUID().toString());
            history.setDeviceId(deviceId);
            history.setEventTime(LocalDateTime.now());
            history.setEventType(eventType);
            history.setDetail(detail);

            historyRepository.save(history);
        }

        //設置設備功率
        private void setDefaultPowerConsumptionRate(Device device) {
            if (device.getType() == null) {
                device.setPowerConsumptionRate(0.0);
                return;
            }
        
            switch (device.getType()) {
                case "冷氣機":
                    device.setPowerConsumptionRate(1.43); // 冷氣機消耗功率 1.43 kW，即 1430 W
                    break;
                case "空氣清淨機":
                    device.setPowerConsumptionRate(0.048); // 空氣清淨機消耗功率 48 W
                    break;
                case "除濕機":
                    device.setPowerConsumptionRate(0.19); // 除濕機消耗功率 190 W
                    break;
                case "燈":
                    device.setPowerConsumptionRate(0.04); // 燈消耗功率 40 W
                    break;
                default:
                    device.setPowerConsumptionRate(0.0); // 默認值
                    break;
            }
        }
}
