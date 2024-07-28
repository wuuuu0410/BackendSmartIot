package com.example.SmartIot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartIot.entity.AirPurifier;

public interface AirPurifierRepository extends JpaRepository<AirPurifier, Long>{

    AirPurifier findByDeviceId(Long id);

}
