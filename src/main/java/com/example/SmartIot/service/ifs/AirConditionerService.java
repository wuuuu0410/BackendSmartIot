package com.example.SmartIot.service.ifs;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

import com.example.SmartIot.entity.AirConditioner;

public interface AirConditionerService {

    List<AirConditioner> getAllAirConditioners();

    AirConditioner getAirConditionerById(Long id);

    ResponseEntity<?> saveAirConditioner(AirConditioner airConditioner);

    ResponseEntity<?> patchAirConditioner(Long id, Map<String, Object> updates);

    ResponseEntity<?> batchPatchAirConditioners(List<Map<String, Object>> updates);

    void deleteAirConditioner(Long id);
}