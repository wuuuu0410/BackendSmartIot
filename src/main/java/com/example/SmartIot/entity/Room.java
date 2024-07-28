package com.example.SmartIot.entity;

import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "room")
public class Room {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String area;
    private String type;
    private Boolean status;

    //一個房間包含多個設備(Device表的room屬性)
    @OneToMany(mappedBy = "room")
    //設備需要被序列化
    @JsonManagedReference
    private Set<Device> devices;


    //constructor
    public Room() {
    }


    public Room(Long id, String name, String area, String type,Boolean status, Set<Device> devices) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.type = type;
        this.status = status;
        this.devices = devices;
    }

    //getters and setters
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Set<Device> getDevices() {
        return this.devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        //預設設備為關閉
        if (this.status == null) {
            this.status = false;
        }
    }

}
