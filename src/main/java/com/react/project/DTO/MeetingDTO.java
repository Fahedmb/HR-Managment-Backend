package com.react.project.DTO;

import com.react.project.Enumirator.MeetingStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDTO {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private String organizerName;
    private List<Long> attendeeIds;
    private List<UserDTO> attendees;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String meetingLink;
    private MeetingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
