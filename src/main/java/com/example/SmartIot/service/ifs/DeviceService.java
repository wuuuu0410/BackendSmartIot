package com.example.SmartIot.service.ifs;

import java.util.List;

import java.util.Set;

import org.springframework.http.ResponseEntity;

import com.example.SmartIot.entity.Device;
import com.example.SmartIot.vo.DeviceReq;


public interface DeviceService {

    //搜尋全部設備
    List<Device> getAllDevices();

    //搜尋特定設備
    Device getDeviceById(Long id);

    //新增或更新設備
    Device saveDevice(DeviceReq deviceReq);

    //搜尋特定設備
    List<Device> searchDevices(String name, String type, String area, Boolean status);

    //刪除設備
    ResponseEntity<String> deleteDevice(Long id);

    //刪除一組設備
    ResponseEntity<String> deleteDevices(List<Long> ids);

    ResponseEntity<String> saveDevices(Set<Device> devices);
}
