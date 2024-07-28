package com.example.SmartIot.vo;

import jakarta.validation.constraints.NotBlank;

public class RoomReq {

    private Long id;
    
    // 如果字符串為空或只包含空白字符，將會觸發驗證異常
    @NotBlank(message = "Room name is mandatory")
    private String name;

    @NotBlank(message = "area is mandatory")
    private String area;//room

    private String type;

    private Boolean status;
    
    //constructor
    public RoomReq() {
    }

    public RoomReq(Long id,@NotBlank(message = "Room name is mandatory") String name, String area, String type,Boolean status) {
        this.id = id;
        this.name = name;
        this.area = area;
        this.type = type;
        this.status = status;
    }

    //getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getType() {
        return type;
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

}
