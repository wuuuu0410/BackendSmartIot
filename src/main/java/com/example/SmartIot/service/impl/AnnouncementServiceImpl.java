package com.example.SmartIot.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartIot.entity.Announcement;
import com.example.SmartIot.entity.Room;
import com.example.SmartIot.repository.AnnouncementRepository;
import com.example.SmartIot.repository.RoomRepository;
import com.example.SmartIot.service.ifs.AnnouncementService;
import com.example.SmartIot.vo.AnnouncementReq;
import com.example.SmartIot.vo.AnnouncementRes;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public AnnouncementRes createAnnouncement(AnnouncementReq request) {
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setPublishTime(LocalDate.now());

        String message;
        if (request.getRoomIds() == null || request.getRoomIds().isEmpty()) {
            // 如果沒有指定房間，則發送給所有房間
            List<Long> allRoomIds = roomRepository.findAllIds();
            announcement.setRoomIds(allRoomIds);
            message = "公告已成功發送給所有房間";
        } else {
            announcement.setRoomIds(request.getRoomIds());
            message = "公告已成功發送給指定的房間";
        }

        Announcement savedAnnouncement = announcementRepository.save(announcement);
        return new AnnouncementRes(savedAnnouncement, message);
    }

    @Override
    public List<Announcement> getAnnouncementsByRoomIdWithRoomInfo(Long roomId) {
        List<Announcement> announcements = announcementRepository.findByRoomIdsContaining(roomId);
        return addRoomInfoToAnnouncements(announcements);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new RuntimeException("找不到 ID 為 " + id + " 的公告");
        }
        announcementRepository.deleteById(id);
    }

    @Override
    public List<Announcement> getAllAnnouncementsWithRoomInfo() {
        List<Announcement> announcements = announcementRepository.findAll();
        return addRoomInfoToAnnouncements(announcements);
    }

    @Override
    public void deleteMultipleAnnouncements(List<Long> ids) {
        List<Announcement> announcements = announcementRepository.findAllById(ids);
        if (announcements.size() != ids.size()) {
            throw new RuntimeException("部分公告不存在");
        }
        announcementRepository.deleteAllById(ids);
    }

    private List<Announcement> addRoomInfoToAnnouncements(List<Announcement> announcements) {
        for (Announcement announcement : announcements) {
            List<Map<String, String>> roomInfo = new ArrayList<>();
            for (Long roomId : announcement.getRoomIds()) {
                Room room = roomRepository.findById(roomId).orElse(null);
                if (room != null) {
                    Map<String, String> info = new HashMap<>();
                    info.put("id", room.getId().toString());
                    info.put("name", room.getName());
                    info.put("area", room.getArea());
                    roomInfo.add(info);
                }
            }
            announcement.setRoomInfo(roomInfo);
        }
        return announcements;
    }
}
