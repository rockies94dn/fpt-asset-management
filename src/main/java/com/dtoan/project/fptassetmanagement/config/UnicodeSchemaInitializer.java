package com.dtoan.project.fptassetmanagement.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
public class UnicodeSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @PostConstruct
    public void ensureUnicodeColumns() {
        if (!isSqlServer()) {
            return;
        }

        jdbcTemplate.execute("""
                IF EXISTS (
                    SELECT 1
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'chat_messages'
                      AND COLUMN_NAME = 'message'
                      AND DATA_TYPE <> 'nvarchar'
                )
                BEGIN
                    ALTER TABLE chat_messages ALTER COLUMN message NVARCHAR(2000) NOT NULL;
                END

                IF EXISTS (
                    SELECT 1
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'users'
                      AND COLUMN_NAME = 'full_name'
                      AND DATA_TYPE <> 'nvarchar'
                )
                BEGIN
                    ALTER TABLE users ALTER COLUMN full_name NVARCHAR(150) NOT NULL;
                END

                IF EXISTS (
                    SELECT 1
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'notifications'
                      AND COLUMN_NAME = 'title'
                      AND DATA_TYPE <> 'nvarchar'
                )
                BEGIN
                    ALTER TABLE notifications ALTER COLUMN title NVARCHAR(150) NOT NULL;
                END

                IF EXISTS (
                    SELECT 1
                    FROM INFORMATION_SCHEMA.COLUMNS
                    WHERE TABLE_NAME = 'notifications'
                      AND COLUMN_NAME = 'message'
                      AND DATA_TYPE <> 'nvarchar'
                )
                BEGIN
                    ALTER TABLE notifications ALTER COLUMN message NVARCHAR(500) NOT NULL;
                END
                """);
    }

    private boolean isSqlServer() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("sql server");
        } catch (Exception exception) {
            return false;
        }
    }
}
