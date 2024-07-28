package com.example.SmartIot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartIot.entity.AirConditioner;

public interface AirConditionerRepository extends JpaRepository<AirConditioner, Long> {

    AirConditioner findByDeviceId(Long id);
}