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

import com.example.SmartIot.entity.Dehumidifier;
import com.example.SmartIot.service.ifs.DehumidifierService;

@CrossOrigin
@RestController
@RequestMapping("/dehumidifiers")
public class DehumidifierController {

    @Autowired
    private DehumidifierService dehumidifierService;

    @GetMapping
    public List<Dehumidifier> getAllDehumidifiers() {
        return dehumidifierService.getAllDehumidifiers();
    }

    @GetMapping("/{id}")
    public Dehumidifier getDehumidifier(@PathVariable("id") Long id) {
        return dehumidifierService.getDehumidifierById(id);
    }

    @PostMapping
    public ResponseEntity<?> saveDehumidifier(@RequestBody Dehumidifier dehumidifier) {
        return dehumidifierService.saveDehumidifier(dehumidifier);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchDehumidifier(@PathVariable("id") Long id, @RequestBody Map<String, Object> updates) {
        return dehumidifierService.patchDehumidifier(id, updates);
    }

    @DeleteMapping("/{id}")
    public void deleteDehumidifier(@PathVariable("id") Long id) {
        dehumidifierService.deleteDehumidifier(id);
    }

    @PatchMapping("/batch")
    public ResponseEntity<?> batchPatchDehumidifiers(@RequestBody List<Map<String, Object>> updates) {
        return dehumidifierService.batchPatchDehumidifiers(updates);
    }
}
