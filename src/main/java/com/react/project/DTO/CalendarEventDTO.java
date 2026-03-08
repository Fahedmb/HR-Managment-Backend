package com.react.project.DTO;

import lombok.*;

/**
 * Unified calendar event returned to the frontend so HR can visualise
 * an employee's schedule inside an approve / reject modal.
 *
 * The {@code type} field tells the frontend which entity this event
 * came from: LEAVE | TIMESHEET | MEETING | TASK
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEventDTO {

    private Long   id;
    private String type;      // LEAVE | TIMESHEET | MEETING | TASK
    private String title;
    /** ISO-8601 date or date-time string (start of event) */
    private String start;
    /** ISO-8601 date or date-time string (end of event, may be null) */
    private String end;
    /** true for all-day events (leave, timesheet); false for timed events (meetings) */
    private boolean allDay;
    private String status;
    /** Extra contextual information (reason for leave, hours worked, etc.) */
    private String notes;
    /** Hex colour hint for the calendar renderer, e.g. "#f87171" */
    private String color;
}
