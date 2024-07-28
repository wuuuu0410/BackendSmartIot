package com.example.SmartIot.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.example.SmartIot.entity.Light;

public interface LightRepository extends JpaRepository<Light, Long> {

    Light findByDeviceId(Long id);

    @Modifying
    @Query("DELETE FROM Light l WHERE l.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);


}
