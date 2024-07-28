package com.example.SmartIot.vo;

import java.sql.Timestamp;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceReq {

    //沒id就代表新增設備,有id代表要更新設備
    @Nullable
    private Long id;
    
    // 如果字符串為空或只包含空白字符，將會觸發驗證異常，並且異常訊息會顯示為Device name is mandatory。
    @NotBlank(message = "Device name is mandatory")
    private String name;

    @NotBlank(message = "Device type is mandatory")
    private String type;

    // 用於驗證字段不能為null。如果字段為null，將會觸發驗證異常，並且異常訊息會顯示為Device status is mandatory。
    @NotNull(message = "Device status is mandatory")
    private Boolean status;

    @NotNull(message = "Device time is mandatory")
    private Timestamp time;

    // 用於標記字段可以是null
    @Nullable
    private Long roomId;

    //constructor
    public DeviceReq() {
    }

    public DeviceReq(
            Long id,
            @NotBlank(message = "Device name is mandatory") String name,
            @NotBlank(message = "Device type is mandatory") String type,
            @NotNull(message = "Device status is mandatory") Boolean status,
            @NotNull(message = "Device time is mandatory") Timestamp time,
            Long roomId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.time = time;
        this.roomId = roomId;
    }

    //getters and setters

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

}
