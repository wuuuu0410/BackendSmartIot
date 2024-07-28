package com.example.SmartIot.constant;

public enum ResMsg {

    SUCCESS(200, "成功啦"),
	BAD_REQUEST(400, "有錯喔"),
	UNAUTHORIZED(401, "只有你 不被允許喔"),
	FORBIDDEN(403, "非請勿入"),
	NOT_FOUND(404, "找不到欸"),
	INTERNAL_SERVERE_ERROR(500, "伺服器錯誤");
	
	private final int code;
	private final String description;
	
	ResMsg(int code, String description){
		this.code = code;
		this.description = description;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getDescription() {
		return description;
	}

}
