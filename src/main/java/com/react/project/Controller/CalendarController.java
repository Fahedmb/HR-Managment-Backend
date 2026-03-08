package com.react.project.Controller;

import com.react.project.DTO.CalendarEventDTO;
import com.react.project.Model.LeaveRequest;
import com.react.project.Model.Meeting;
import com.react.project.Model.Task;
import com.react.project.Model.TimeSheet;
import com.react.project.Repository.LeaveRequestRepository;
import com.react.project.Repository.MeetingRepository;
import com.react.project.Repository.TaskRepository;
import com.react.project.Repository.TimeSheetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Provides a unified calendar feed for a user.
 * Used by the HR approval / rejection modal so that reviewers can
 * visualise what the employee already has scheduled before making a
 * decision.
 *
 * GET /api/calendar/user/{userId}
 *   → list of CalendarEventDTO (leave, timesheets, meetings, tasks)
 */
@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final LeaveRequestRepository  leaveRepo;
    private final TimeSheetRepository     timesheetRepo;
    private final MeetingRepository       meetingRepo;
    private final TaskRepository          taskRepo;

    // Colour palette keyed by status / type so the frontend can render
    // consistently without its own mapping logic.
    private static final Map<String, String> LEAVE_COLORS = Map.of(
            "PENDING",   "#facc15",  // yellow
            "APPROVED",  "#4ade80",  // green
            "REJECTED",  "#f87171",  // red
            "CANCELLED", "#94a3b8"   // slate
    );
    private static final Map<String, String> TS_COLORS = Map.of(
            "PENDING",  "#facc15",
            "APPROVED", "#4ade80",
            "REJECTED", "#f87171"
    );

    /**
     * Returns all calendar events for the given user:
     * leave requests, timesheet entries, meetings, and tasks with deadlines.
     */
    @GetMapping("/user/{userId}")
    public List<CalendarEventDTO> getUserCalendar(@PathVariable Long userId) {
        List<CalendarEventDTO> events = new ArrayList<>();

        // ── Leave requests ────────────────────────────────────────────────
        for (LeaveRequest lr : leaveRepo.findByUserId(userId)) {
            String status = lr.getStatus() != null ? lr.getStatus().name() : "UNKNOWN";
            events.add(CalendarEventDTO.builder()
                    .id(lr.getId())
                    .type("LEAVE")
                    .title(leaveTitle(lr))
                    .start(lr.getStartDate().toString())
                    .end(lr.getEndDate().toString())
                    .allDay(true)
                    .status(status)
                    .notes(lr.getReason())
                    .color(LEAVE_COLORS.getOrDefault(status, "#94a3b8"))
                    .build());
        }

        // ── Timesheet entries ─────────────────────────────────────────────
        for (TimeSheet ts : timesheetRepo.findByUserId(userId)) {
            String status = ts.getStatus() != null ? ts.getStatus().name() : "UNKNOWN";
            events.add(CalendarEventDTO.builder()
                    .id(ts.getId())
                    .type("TIMESHEET")
                    .title(ts.getHoursWorked() + "h worked")
                    .start(ts.getDate().toString())
                    .end(ts.getDate().toString())
                    .allDay(true)
                    .status(status)
                    .notes(null)
                    .color(TS_COLORS.getOrDefault(status, "#94a3b8"))
                    .build());
        }

        // ── Meetings ──────────────────────────────────────────────────────
        for (Meeting m : meetingRepo.findAllForUser(userId)) {
            events.add(CalendarEventDTO.builder()
                    .id(m.getId())
                    .type("MEETING")
                    .title(m.getTitle())
                    .start(m.getStartTime().toString())
                    .end(m.getEndTime().toString())
                    .allDay(false)
                    .status(m.getStatus() != null ? m.getStatus().name() : "SCHEDULED")
                    .notes(m.getDescription())
                    .color("#60a5fa")    // blue
                    .build());
        }

        // ── Tasks with deadlines ──────────────────────────────────────────
        for (Task t : taskRepo.findByAssignedToId(userId)) {
            if (t.getDeadline() == null) continue;
            String status = t.getStatus() != null ? t.getStatus().name() : "UNKNOWN";
            events.add(CalendarEventDTO.builder()
                    .id(t.getId())
                    .type("TASK")
                    .title("Task: " + t.getTitle())
                    .start(t.getDeadline().toString())
                    .end(t.getDeadline().toString())
                    .allDay(true)
                    .status(status)
                    .notes(t.getDescription())
                    .color("#c084fc")    // purple
                    .build());
        }

        // Sort chronologically by start date
        events.sort(Comparator.comparing(CalendarEventDTO::getStart));
        return events;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String leaveTitle(LeaveRequest lr) {
        String type = lr.getType() != null ? lr.getType().name().replace("_", " ") : "Leave";
        return (Boolean.TRUE.equals(lr.getHalfDay()) ? "Half-day " : "") + type + " Leave";
    }
}
