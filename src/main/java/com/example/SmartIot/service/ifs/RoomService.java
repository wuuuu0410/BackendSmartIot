package com.example.SmartIot.service.ifs;

import com.example.SmartIot.entity.Room;
import com.example.SmartIot.vo.RoomReq;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;

public interface RoomService {

    List<Room> getAllRooms();

    Room getRoomById(Long id);

    List<Room> searchRooms(String name, String type, String area, Boolean status);

    Room createRoom(@Valid RoomReq roomReq);

    ResponseEntity<String> deleteRoom(Long id);

    ResponseEntity<String> deleteRooms(List<Long> ids);
}
