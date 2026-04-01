package com.dtoan.project.fptassetmanagement.enums;

public enum AssetStatus {
    AVAILABLE("Sẵn sàng", "success"),
    IN_USE("Đang sử dụng", "primary"),
    BROKEN("Hỏng", "danger"),
    MAINTENANCE("Bảo trì", "warning"),
    LOST("Thất lạc", "secondary");

    private final String displayName;
    private final String badgeClass;

    AssetStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() { return displayName; }
    public String getBadgeClass() { return badgeClass; }
}
