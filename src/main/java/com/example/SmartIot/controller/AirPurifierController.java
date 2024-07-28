package com.example.SmartIot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartIot.entity.AirPurifier;
import com.example.SmartIot.service.ifs.AirPurifierService;

@CrossOrigin
@RestController
@RequestMapping("/air-purifiers")
public class AirPurifierController {

    @Autowired
    private AirPurifierService airPurifierService;

    // 獲取所有空氣清潔器
    @GetMapping
    public List<AirPurifier> getAllAirPurifiers() {
        return airPurifierService.getAllAirPurifiers();
    }

    // 獲取特定的空氣清潔器
    @GetMapping("/{id}")
    public AirPurifier getAirPurifier(@PathVariable("id") Long id) {
        return airPurifierService.getAirPurifierById(id);
    }

    // 新增或修改空氣清潔器
    @PostMapping
    public ResponseEntity<?> saveAirPurifier(@RequestBody AirPurifier airPurifier) {
        // save 方法會判定有無id，決定創建或修改
        return airPurifierService.saveAirPurifier(airPurifier);
    }

    // 刪除空氣清潔器
    @DeleteMapping("/{id}")
    public void deleteAirPurifier(@PathVariable("id") Long id) {
        airPurifierService.deleteAirPurifier(id);
    }

    // 部分更新空氣清潔器
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchAirPurifier(@PathVariable("id") Long id, @RequestBody Map<String, Object> updates) {
        return airPurifierService.patchAirPurifier(id, updates);
    }

    // 批次更新空氣清潔器
    @PatchMapping("/batch")
    public ResponseEntity<?> batchPatchAirPurifiers(@RequestBody List<Map<String, Object>> updates) {
        return airPurifierService.batchPatchAirPurifiers(updates);
    }
}
