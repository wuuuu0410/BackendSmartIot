package com.example.SmartIot.vo;

import java.util.List;

public class AnnouncementReq {
    private String title;
    private String content;
    private List<Long> roomIds;

    public AnnouncementReq() {
    }

    public AnnouncementReq(String title, String content, List<Long> roomIds) {
        this.title = title;
        this.content = content;
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

    public List<Long> getRoomIds() {
        return roomIds;
    }

    public void setRoomIds(List<Long> roomIds) {
        this.roomIds = roomIds;
    }

}
