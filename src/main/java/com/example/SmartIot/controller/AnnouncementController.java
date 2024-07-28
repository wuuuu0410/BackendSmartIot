package com.example.SmartIot.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SmartIot.entity.Announcement;
import com.example.SmartIot.service.ifs.AnnouncementService;
import com.example.SmartIot.vo.AnnouncementReq;
import com.example.SmartIot.vo.AnnouncementRes;

@CrossOrigin
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<AnnouncementRes> createAnnouncement(@RequestBody AnnouncementReq request) {
        AnnouncementRes response = announcementService.createAnnouncement(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<Announcement>> getAnnouncementsByRoomId(@PathVariable("id") Long id) {
        List<Announcement> announcements = announcementService.getAnnouncementsByRoomIdWithRoomInfo(id);
        if (announcements.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(announcements);
        }
        return ResponseEntity.ok(announcements);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable("id") Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok("公告已成功刪除");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Announcement>> getAllAnnouncements() {
        List<Announcement> announcements = announcementService.getAllAnnouncementsWithRoomInfo();
        return ResponseEntity.ok(announcements);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteMultipleAnnouncements(@RequestBody List<Long> ids) {
        try {
            announcementService.deleteMultipleAnnouncements(ids);
            return ResponseEntity.ok("選定的公告已成功刪除");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}