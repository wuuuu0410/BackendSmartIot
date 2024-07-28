package com.example.SmartIot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.SmartIot.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>{

    @Query("SELECT r FROM Room r WHERE " +
            "(:name IS NULL OR r.name LIKE %:name%) AND " +
            "(:type IS NULL OR r.type LIKE %:type%) AND " +
            "(:area IS NULL OR r.area LIKE %:area%) AND " +
            "(:status IS NULL OR r.status = :status)")
     List<Room> findByCriteria(@Param("name") String name,
                               @Param("type") String type,
                               @Param("area") String area,
                               @Param("status") Boolean status);

    Room findByArea(String area);

    @Query("SELECT r.id FROM Room r")
    List<Long> findAllIds();
}
