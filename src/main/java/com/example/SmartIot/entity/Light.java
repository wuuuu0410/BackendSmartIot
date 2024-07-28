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
@Table(name = "light")
public class Light {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int brightness;
    private int color_temp;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    @JsonBackReference
    private Device device;

    //constructor
    public Light() {
    }


    public Light(Long id, int brightness, int color_temp, Device device) {
        this.id = id;
        this.brightness = brightness;
        this.color_temp = color_temp;
        this.device = device;
    }

    //getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getBrightness() {
        return this.brightness;
    }

    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getColor_temp() {
        return this.color_temp;
    }

    public void setColor_temp(int color_temp) {
        this.color_temp = color_temp;
    }

    public Device getDevice() {
        return this.device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

}
