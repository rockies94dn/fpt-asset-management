package com.dtoan.project.fptassetmanagement.enums;

public enum UsageStatus {
    ACTIVE("Đang sử dụng"),
    COMPLETED("Đã hoàn trả"),
    CANCELLED("Đã hủy");

    private final String displayName;

    UsageStatus(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }
}
