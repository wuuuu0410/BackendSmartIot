package com.example.SmartIot.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import com.example.SmartIot.utility.HashMapConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;



@Entity
@Table(name = "history")
public class History {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private String eventId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "event_time", nullable = false, updatable = false)
    private LocalDateTime eventTime;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Convert(converter = HashMapConverter.class)
    @Column(name = "detail")
    private Map<String, Object> detail;

    //constructor
    public History() {
    }

    public History(Long id, String eventId, Long deviceId, LocalDateTime eventTime, String eventType,
            Map<String, Object> detail) {
        this.id = id;
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.eventTime = eventTime;
        this.eventType = eventType;
        this.detail = detail;
    }

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getDetail() {
        return detail;
    }

    public void setDetail(Map<String, Object> detail) {
        this.detail = detail;
    }

    @PrePersist
    public void prePersist() {
        this.eventId = UUID.randomUUID().toString();
        if(eventTime == null){
            this.eventTime = LocalDateTime.now();
        }
    }
    
}
