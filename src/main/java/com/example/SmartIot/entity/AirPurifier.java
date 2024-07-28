package com.example.SmartIot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "air_purifier")
public class AirPurifier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int air_quality;
    private int fan_speed;
    private Double operating_time;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private Device device;

    //constructor
    public AirPurifier() {
    }

    public AirPurifier(Long id, int air_quality, int fan_speed, Double operating_time, Device device) {
        this.id = id;
        this.air_quality = air_quality;
        this.fan_speed = fan_speed;
        this.operating_time = operating_time;
        this.device = device;
    }
    
    //getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getAir_quality() {
        return this.air_quality;
    }

    public void setAir_quality(int air_quality) {
        this.air_quality = air_quality;
    }

    public int getFan_speed() {
        return this.fan_speed;
    }

    public void setFan_speed(int fan_speed) {
        this.fan_speed = fan_speed;
    }

    public Double getOperating_time() {
        return this.operating_time;
    }

    public void setOperating_time(Double operating_time) {
        this.operating_time = operating_time;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
