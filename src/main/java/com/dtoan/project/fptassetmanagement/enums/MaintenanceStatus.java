package com.dtoan.project.fptassetmanagement.enums;

public enum MaintenanceStatus {
    PENDING("Chờ tiếp nhận", "warning"),
    IN_PROGRESS("Đang xử lý", "primary"),
    RESOLVED("Đã giải quyết", "success"),
    CANCELLED("Đã hủy", "secondary");

    private final String displayName;
    private final String badgeClass;

    MaintenanceStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClass() { return badgeClass; }
}
