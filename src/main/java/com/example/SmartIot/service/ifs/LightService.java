package com.example.SmartIot.service.ifs;

import java.util.List;

import java.util.Map;
import org.springframework.http.ResponseEntity;

import com.example.SmartIot.entity.Light;

public interface LightService {

    List<Light> getAllLights();

    Light getLightById(Long id);

    ResponseEntity<?> saveLight(Light light);

    ResponseEntity<?> patchLight(Long id, Map<String, Object> updates);

    ResponseEntity<?> batchPatchLights(List<Map<String, Object>> updates);

    void deleteLight(Long id);

    void deleteLights(List<Long> ids);

}
