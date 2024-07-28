package com.example.SmartIot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartIot.entity.Device;
import com.example.SmartIot.service.ifs.DeviceService;
import com.example.SmartIot.vo.DeviceReq;

@CrossOrigin
@RestController
@RequestMapping("/")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;

    //返回所有設備的列表
    @GetMapping("/devices")
    public List<Device> getAllDevices(){
        return deviceService.getAllDevices();
    }

    //返回特定 id 的設備
    @GetMapping("/devices/{id}")
    public Device getDeviceById(@PathVariable("id") Long id) {
        return deviceService.getDeviceById(id);
    }

    //找設備名或找設備類型
    //範例1: http://localhost:8080/devices/search?status=1 搜尋啟動中的設備
    //範例2: http://localhost:8080/devices/search?name=冷氣機1號&type=冷氣機 多筆要加&
    @GetMapping("/devices/search")
    public List<Device> searchDevices(@RequestParam(name = "name",required = false) String name,
                                      @RequestParam(name = "type",required = false) String type,
                                      @RequestParam(name = "area",required = false) String area,
                                      @RequestParam(name = "status",required = false) Boolean status){
        return deviceService.searchDevices(name, type, area, status);
    }

    //從 Request 中讀取 JSON 資料並創建一個新的設備
    @PostMapping("/devices")
    public Device create(@RequestBody DeviceReq deviceReq) {
        System.out.println(deviceReq);
        return deviceService.saveDevice(deviceReq);
    }

    //刪除指定 ID 的設備
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<String> deleteDevice(@PathVariable("id") Long id) {
        return deviceService.deleteDevice(id);
    }

    @DeleteMapping("/devices")
    public ResponseEntity<String> deleteDevices(@RequestBody List<Long> ids){
        return deviceService.deleteDevices(ids);
    }

}
