package com.example.SmartIot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.SmartIot.entity.Device;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long>{

    @Query("SELECT d FROM Device d LEFT JOIN FETCH d.room r WHERE " +
           "(:name IS NULL OR d.name LIKE %:name%) AND " +
           "(:type IS NULL OR d.type LIKE %:type%) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:area IS NULL OR r.area LIKE %:area%)")
    List<Device> findByCriteria(@Param("name") String name,
                                @Param("type") String type,
                                @Param("area") String area,
                                @Param("status") Boolean status);

       List<Device> findByRoom_Id(Long roomId);
}
