package com.react.project.Config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * Runs once at startup to apply schema tweaks that Hibernate's
 * ddl-auto=update won't perform automatically (e.g. dropping NOT NULL
 * constraints so that deleted-user FK columns can be set to NULL).
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class SchemaMigrator {

    private final DataSource dataSource;

    @PostConstruct
    public void migrate() {
        String[] statements = {
            // Allow tasks to remain after their creator is deleted
            "ALTER TABLE task MODIFY COLUMN created_by BIGINT NULL",
            // Allow task comments to remain after their author is deleted
            "ALTER TABLE task_comment MODIFY COLUMN author_id BIGINT NULL",
            // Allow performance evaluations to remain after the evaluator is deleted
            "ALTER TABLE performance_evaluation MODIFY COLUMN evaluator_id BIGINT NULL",
            // Allow meetings to remain after their organizer is deleted
            "ALTER TABLE meeting MODIFY COLUMN organizer_id BIGINT NULL",
            // Allow reports to remain after their generator is deleted
            "ALTER TABLE report MODIFY COLUMN generated_by BIGINT NULL",
            // Ensure timesheet_schedule.status is stored as VARCHAR (not MySQL ENUM)
            "ALTER TABLE timesheet_schedule MODIFY COLUMN status VARCHAR(50) NULL"
        };

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                try {
                    stmt.execute(sql);
                } catch (Exception ignored) {
                    // Already nullable – nothing to do
                }
            }
        } catch (Exception e) {
            System.err.println("[SchemaMigrator] Could not obtain connection: " + e.getMessage());
        }
    }
}
