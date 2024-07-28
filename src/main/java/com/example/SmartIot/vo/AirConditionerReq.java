package com.example.SmartIot.vo;

import com.example.SmartIot.constant.AirConditionerConstants;
import com.example.SmartIot.constant.AirConditionerConstants.Mode;
import com.example.SmartIot.constant.AirConditionerConstants.FanSpeed;

public class AirConditionerReq {

    private Long id;
    private Boolean isOn;
    private Double targetTemp;
    private Mode mode;
    private FanSpeed fanSpeed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean isOn() {
        return isOn;
    }

    public void setOn(Boolean on) {
        isOn = on;
    }

    public Double getTargetTemp() {
        return targetTemp;
    }

    public void setTargetTemp(Double targetTemp) {
        this.targetTemp = targetTemp;
    }

    public AirConditionerConstants.Mode getMode() {
        return mode;
    }

    public void setMode(AirConditionerConstants.Mode mode) {
        this.mode = mode;
    }

    public AirConditionerConstants.FanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public void setFanSpeed(AirConditionerConstants.FanSpeed fanSpeed) {
        this.fanSpeed = fanSpeed;
    }
}
