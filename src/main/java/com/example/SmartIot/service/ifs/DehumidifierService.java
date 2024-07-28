package com.example.SmartIot.service.ifs;

import java.util.List;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.example.SmartIot.entity.Dehumidifier;

public interface DehumidifierService {

    List<Dehumidifier> getAllDehumidifiers();

    Dehumidifier getDehumidifierById(Long id);

    ResponseEntity<?> saveDehumidifier(Dehumidifier dehumidifier);

    ResponseEntity<?> patchDehumidifier(Long id, Map<String, Object> updates);

    ResponseEntity<?> batchPatchDehumidifiers(List<Map<String, Object>> updates);

    void deleteDehumidifier(Long id);
}
