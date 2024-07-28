package com.example.SmartIot.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SmartIot.entity.Dehumidifier;

public interface DehumidifierRepository extends JpaRepository<Dehumidifier, Long>{

    Dehumidifier findByDeviceId(Long id);

}
