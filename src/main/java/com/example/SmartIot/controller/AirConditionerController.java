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

import com.example.SmartIot.entity.AirConditioner;
import com.example.SmartIot.service.ifs.AirConditionerService;

@CrossOrigin
@RestController
@RequestMapping("/air-conditioners")
public class AirConditionerController {

    @Autowired
    private AirConditionerService airConditionerService;

    // 獲取所有空調機
    @GetMapping
    public List<AirConditioner> getAllAirConditioners() {
        return airConditionerService.getAllAirConditioners();
    }

    // 獲取特定的空調機
    @GetMapping("/{id}")
    public AirConditioner getAirConditioner(@PathVariable("id") Long id) {
        return airConditionerService.getAirConditionerById(id);
    }

    // 新增或修改空調機
    @PostMapping
    public ResponseEntity<?> saveAirConditioner(@RequestBody AirConditioner airConditioner) {
        return airConditionerService.saveAirConditioner(airConditioner);
    }

    // 部分更新空調機
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchAirConditioner(@PathVariable("id") Long id,
            @RequestBody Map<String, Object> updates) {
        return airConditionerService.patchAirConditioner(id, updates);
    }

    // 刪除空調機
    @DeleteMapping("/{id}")
    public void deleteAirConditioner(@PathVariable("id") Long id) {
        airConditionerService.deleteAirConditioner(id);
    }

    // 批次更新空調機
    @PatchMapping("/batch")
    public ResponseEntity<?> batchPatchAirConditioners(@RequestBody List<Map<String, Object>> updates) {
        return airConditionerService.batchPatchAirConditioners(updates);
    }
}