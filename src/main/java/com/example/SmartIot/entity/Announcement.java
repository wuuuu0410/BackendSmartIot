package com.example.SmartIot.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "announcement")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    private List<Long> roomIds;

    private String title;

    private String content;

    @Column(name = "publish_time")
    private LocalDate publishTime;

    @Transient
    private List<Map<String, String>> roomInfo;

    // constructor
    public Announcement() {
    }

    public Announcement(Long id, List<Long> roomIds, String title, String content, LocalDate publishTime) {
        this.id = id;
        this.roomIds = roomIds;
        this.title = title;
        this.content = content;
        this.publishTime = publishTime;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Long> roomIds) {
        this.roomIds = roomIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDate getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDate publishTime) {
        this.publishTime = publishTime;
    }

    public List<Map<String, String>> getRoomInfo() {
        return roomInfo;
    }

    public void setRoomInfo(List<Map<String, String>> roomInfo) {
        this.roomInfo = roomInfo;
    }
}
