package com.example.SmartIot.vo;

import java.time.LocalDate;
import java.util.List;

import com.example.SmartIot.entity.Announcement;

public class AnnouncementRes {
    private Long id;
    private String title;
    private String content;
    private List<Long> roomIds;
    private LocalDate publishTime;
    private String message;

    public AnnouncementRes(Announcement announcement, String message) {
        this.id = announcement.getId();
        this.title = announcement.getTitle();
        this.content = announcement.getContent();
        this.roomIds = announcement.getRoomIds();
        this.publishTime = announcement.getPublishTime();
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Long> roomIds) {
        this.roomIds = roomIds;
    }

    public LocalDate getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDate publishTime) {
        this.publishTime = publishTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}