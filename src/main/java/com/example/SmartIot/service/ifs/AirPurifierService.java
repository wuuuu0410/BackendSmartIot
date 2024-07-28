package com.example.SmartIot.service.ifs;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

import com.example.SmartIot.entity.AirPurifier;

public interface AirPurifierService {

    List<AirPurifier> getAllAirPurifiers();

    AirPurifier getAirPurifierById(Long id);

    ResponseEntity<?> saveAirPurifier(AirPurifier airPurifier);

    ResponseEntity<?> patchAirPurifier(Long id, Map<String, Object> updates);

    ResponseEntity<?> batchPatchAirPurifiers(List<Map<String, Object>> updates);

    void deleteAirPurifier(Long id);
}
