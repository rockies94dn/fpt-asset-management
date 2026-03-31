package com.dtoan.project.fptassetmanagement.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AppNotification {
    private final Long id;
    private final String key;
    private final String title;
    private final String message;
    private final String icon;
    private final String tone;
    private final String href;
    private final long count;
    private final boolean read;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
