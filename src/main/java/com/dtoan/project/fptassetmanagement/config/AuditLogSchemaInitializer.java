package com.dtoan.project.fptassetmanagement.config;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class AuditLogSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void ensureUnicodeAuditLogSummary() {
        jdbcTemplate.execute("""
                IF EXISTS (
                    SELECT 1
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'audit_logs'
                      AND COLUMN_NAME = 'summary'
                      AND DATA_TYPE <> 'nvarchar'
                )
                BEGIN
                    ALTER TABLE audit_logs ALTER COLUMN summary NVARCHAR(500) NOT NULL;
                END
                """);
    }
}
