package com.example.SmartIot.service.impl;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.SmartIot.entity.Device;
import com.example.SmartIot.entity.History;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.DeviceRepository;
import com.example.SmartIot.repository.HistoryRepository;
import com.example.SmartIot.repository.RoomRepository;
import com.example.SmartIot.service.ifs.DeviceService;
import com.example.SmartIot.service.ifs.RoomService;
import com.example.SmartIot.vo.RoomReq;

import jakarta.transaction.Transactional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HistoryRepository historyRepository;
    private final DeviceService deviceService;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository, HistoryRepository historyRepository,DeviceService deviceService) {
        this.roomRepository = roomRepository;
        this.historyRepository = historyRepository;
        this.deviceService = deviceService;
    }

    @Override
    @Transactional
    public List<Room> getAllRooms() {
        //找房間所有的電器
        List<Room> rooms = roomRepository.findAll();
        for (Room room : rooms) {
            Set<Device> devices = room.getDevices();
            boolean roomStatus = devices.stream().anyMatch(Device::isStatus);
            room.setStatus(roomStatus);
        }
        roomRepository.saveAll(rooms);
        return rooms;
    }

    @Override
    public Room getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        sortAndGroupDevices(room);
        return room;
    }

    @Override
    public List<Room> searchRooms(String name, String type, String area, Boolean status){
        return roomRepository.findByCriteria(name, type, area, status);
    }

    @Override
    @Transactional
    public Room createRoom(RoomReq roomReq) {
        Room room;
        boolean isNew = false;
        if (roomReq.getId() != null) {
            room = roomRepository.findById(roomReq.getId())
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            // 更新房間狀態
            room.setStatus(roomReq.getStatus());
            if (Boolean.FALSE.equals(roomReq.getStatus())) {
                Set<Device> devices = room.getDevices();
                if (devices != null) {
                    for (Device device : devices) {
                        device.setStatus(false);
                    }
                } else {
                    throw new RuntimeException("Devices not found");
                }
            }
        } else {
            // 再確定area是否存在
            Room existingRoom = roomRepository.findByArea(roomReq.getArea());
            if (existingRoom != null) {
                // 更新现有房间的状态
                existingRoom.setName(roomReq.getName());
                existingRoom.setType(roomReq.getType());
                existingRoom.setStatus(roomReq.getStatus());
            
                if (Boolean.FALSE.equals(roomReq.getStatus())) {
                    Set<Device> devices = existingRoom.getDevices();
                    if (devices != null) {
                        for (Device device : devices) {
                            device.setStatus(false);
                        }
                    } else {
                        throw new RuntimeException("Devices not found");
                    }
                }
                room = roomRepository.save(existingRoom);
            } else {
                // 創建新房間
                room = new Room();
                room.setName(roomReq.getName());
                room.setArea(roomReq.getArea());
                room.setType(roomReq.getType());
                room.setStatus(roomReq.getStatus());
    
                // 初始化設備
                if (room.getDevices() == null) {
                    room.setDevices(new LinkedHashSet<>());
                }
                isNew = true;
            }
        }
    
        Room savedRoom = roomRepository.save(room);

        if (isNew) {
            saveHistoryRecord(savedRoom.getId(), "新增房間", Map.of("roomName", savedRoom.getName(), "roomType", savedRoom.getType(), "roomArea", savedRoom.getArea()));
        }

        return savedRoom;
 
    }

    @Override
    @Transactional
    public ResponseEntity<String> deleteRoom(Long id) {
        try{
            Room room = roomRepository.findById(id).orElseThrow(() -> new RuntimeException("Room not found"));
        
            // 清除房間內所有設備的關聯
            for (Device device : room.getDevices()) {
                device.setRoom(null);
            }
            deviceService.saveDevices(room.getDevices());

            roomRepository.deleteById(id);
        
            // 記錄刪除房間的歷史紀錄
            saveHistoryRecord(room.getId(), "刪除房間", Map.of("roomName", room.getName(), "roomType", room.getType(), "roomArea", room.getArea()));
        
            return ResponseEntity.ok("Room deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete room: " + e.getMessage());
        }
    }

    //刪除多台設備
    @Override
    @Transactional
    public ResponseEntity<String> deleteRooms(List<Long> ids) {
        try{
            List<Room> rooms = roomRepository.findAllById(ids);
        
            // 清除房間內所有設備的關聯
            for (Room room : rooms) {
                for (Device device : room.getDevices()) {
                    device.setRoom(null);
                }
                deviceService.saveDevices(room.getDevices());
            }
            
            roomRepository.deleteAll(rooms);
            
            // 記錄刪除房間的歷史紀錄
            for (Room room : rooms) {
                saveHistoryRecord(room.getId(), "刪除房間", Map.of("roomName", room.getName(), "roomType", room.getType(), "roomArea", room.getArea()));
            }
            return ResponseEntity.ok("Rooms deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete rooms: " + e.getMessage());
        }
    }


    private void sortAndGroupDevices(Room room) {
        Map<String, List<Device>> groupedDevices = room.getDevices().stream()
                .sorted(Comparator.comparing(Device::getId))
                .collect(Collectors.groupingBy(Device::getType));

        Set<Device> sortedDevices = groupedDevices.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        room.setDevices(sortedDevices);
    }

    private void saveHistoryRecord(Long entityId, String eventType, Map<String, Object> detail) {
        History history = new History();
        history.setEventId(UUID.randomUUID().toString());
        history.setDeviceId(entityId); // or roomId, depending on your schema
        history.setEventTime(LocalDateTime.now());
        history.setEventType(eventType);
        history.setDetail(detail);

        historyRepository.save(history);
    }
}