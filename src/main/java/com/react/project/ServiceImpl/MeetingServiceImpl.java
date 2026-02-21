package com.react.project.ServiceImpl;

import com.react.project.DTO.MeetingDTO;
import com.react.project.DTO.UserDTO;
import com.react.project.Enumirator.MeetingStatus;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Exception.UserException;
import com.react.project.Model.Meeting;
import com.react.project.Model.User;
import com.react.project.Repository.MeetingRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.MeetingService;
import com.react.project.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    public List<MeetingDTO> findAll() {
        return meetingRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public MeetingDTO findById(Long id) {
        return toDTO(meetingRepository.findById(id)
                .orElseThrow(() -> new UserException("Meeting not found")));
    }

    @Override
    public List<MeetingDTO> findForUser(Long userId) {
        return meetingRepository.findAllForUser(userId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MeetingDTO> findOrganizedBy(Long organizerId) {
        return meetingRepository.findByOrganizerId(organizerId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public MeetingDTO create(MeetingDTO dto) {
        User organizer = userRepository.findById(dto.getOrganizerId())
                .orElseThrow(() -> new UserException("Organizer not found"));
        List<User> attendees = dto.getAttendeeIds() != null
                ? userRepository.findAllById(dto.getAttendeeIds())
                : List.of();

        Meeting meeting = Meeting.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .organizer(organizer)
                .attendees(attendees)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .meetingLink(dto.getMeetingLink())
                .status(MeetingStatus.SCHEDULED)
                .build();

        Meeting saved = meetingRepository.save(meeting);

        // Notify all attendees
        attendees.forEach(a -> notificationService.create(
                a.getId(),
                organizer.getFirstName() + " scheduled a meeting '" + saved.getTitle()
                        + "' on " + saved.getStartTime().toLocalDate() + " at " + saved.getStartTime().toLocalTime() + ".",
                NotificationType.MEETING_SCHEDULED));

        return toDTO(saved);
    }

    @Override
    public MeetingDTO update(Long id, MeetingDTO dto) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new UserException("Meeting not found"));
        meeting.setTitle(dto.getTitle());
        meeting.setDescription(dto.getDescription());
        meeting.setStartTime(dto.getStartTime());
        meeting.setEndTime(dto.getEndTime());
        meeting.setLocation(dto.getLocation());
        meeting.setMeetingLink(dto.getMeetingLink());
        if (dto.getAttendeeIds() != null) {
            meeting.setAttendees(userRepository.findAllById(dto.getAttendeeIds()));
        }
        Meeting saved = meetingRepository.save(meeting);
        saved.getAttendees().forEach(a -> notificationService.create(a.getId(),
                "Meeting '" + saved.getTitle() + "' has been updated.",
                NotificationType.MEETING_UPDATED));
        return toDTO(saved);
    }

    @Override
    public MeetingDTO cancel(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new UserException("Meeting not found"));
        meeting.setStatus(MeetingStatus.CANCELLED);
        Meeting saved = meetingRepository.save(meeting);
        saved.getAttendees().forEach(a -> notificationService.create(a.getId(),
                "Meeting '" + saved.getTitle() + "' has been cancelled.",
                NotificationType.MEETING_CANCELLED));
        return toDTO(saved);
    }

    @Override
    public void delete(Long id) {
        meetingRepository.deleteById(id);
    }

    private MeetingDTO toDTO(Meeting m) {
        return MeetingDTO.builder()
                .id(m.getId())
                .title(m.getTitle())
                .description(m.getDescription())
                .organizerId(m.getOrganizer().getId())
                .organizerName(m.getOrganizer().getFirstName() + " " + m.getOrganizer().getLastName())
                .attendeeIds(m.getAttendees().stream().map(User::getId).collect(Collectors.toList()))
                .attendees(m.getAttendees().stream().map(u -> UserDTO.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .position(u.getPosition())
                        .department(u.getDepartment())
                        .role(u.getRole())
                        .build()).collect(Collectors.toList()))
                .startTime(m.getStartTime())
                .endTime(m.getEndTime())
                .location(m.getLocation())
                .meetingLink(m.getMeetingLink())
                .status(m.getStatus())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build();
    }
}
