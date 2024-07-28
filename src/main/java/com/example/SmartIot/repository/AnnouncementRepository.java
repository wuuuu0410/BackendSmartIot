package com.example.SmartIot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SmartIot.entity.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 根據房間 ID 查找公告
    List<Announcement> findByRoomIdsContaining(Long roomId);

}