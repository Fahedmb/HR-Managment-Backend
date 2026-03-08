package com.react.project.ServiceImpl;

import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;
import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.Enumirator.Role;
import com.react.project.Exception.UserException;
import com.react.project.Model.User;
import com.react.project.Repository.*;
import com.react.project.Service.EmailService;
import com.react.project.Service.UserService;
import com.react.project.DTO.UserDTO;
import com.react.project.Service.JwtService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    @Autowired
    private EmailService emailService;

    // Repos needed to cleanly cascade-delete a user
    private final NotificationRepository       notificationRepository;
    private final LeaveRequestRepository       leaveRequestRepository;
    private final TimeSheetRepository          timeSheetRepository;
    private final TimesheetScheduleRepository  timesheetScheduleRepository;
    private final TeamMemberRepository         teamMemberRepository;
    private final TaskRepository               taskRepository;
    private final TaskCommentRepository        taskCommentRepository;
    private final MeetingRepository            meetingRepository;
    private final PerformanceEvaluationRepository performanceEvaluationRepository;
    private final ChatMessageRepository        chatMessageRepository;
    private final ReportRepository             reportRepository;
    private final TeamRepository               teamRepository;
    private final ProjectRepository            projectRepository;

    @Override
    public UserDTO findById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public User getUserEntityById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void incrementUsedLeaveDays(Long userId, int days) {
        User user = getUserEntityById(userId);
        user.setUsedDaysThisYear(user.getUsedDaysThisYear() + days);
        userRepository.save(user);
    }

    @Override
    public void decrementUsedLeaveDays(Long userId, int days) {
        User user = getUserEntityById(userId);
        int updated = Math.max(0, user.getUsedDaysThisYear() - days);
        user.setUsedDaysThisYear(updated);
        userRepository.save(user);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        return findById(userId);
    }

    @Override
    public List<UserDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findByDepartment(String department) {
        return userRepository.findByDepartment(department)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findByRole(Role role) {
        return userRepository.findByRole(role)
                .stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO changeRole(Long id, Role newRole) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));
        user.setRole(newRole);
        return convertToDTO(userRepository.save(user));
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // Auto-derive username from firstName + "." + lastName
        String baseUsername = (request.getFirstName() + "." + request.getLastName()).toLowerCase().replaceAll("\\s+", "");
        String username = baseUsername;
        int suffix = 1;
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + suffix++;
        }

        User user = User.builder()
                .username(username)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .department(request.getDepartment())
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                .build();
        userRepository.save(user);

        // Generate JWT so the user is immediately logged-in after registration
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        String token = jwtService.generateToken(Map.of("role", user.getRole().toString(), "username", user.getUsername()), userDetails, 1000L * 60 * 60);

        Map<String, Object> templateModel = Map.of(
                "username", user.getFirstName() + " " + user.getLastName(),
                "loginUrl", "http://localhost:5173/login"
        );

        try {
            emailService.sendEmail(user.getEmail(), "Welcome to HR Management System!", "registrationEmail", templateModel);
        } catch (Exception e) {
            // Catch ALL exceptions (including Spring MailAuthenticationException which extends
            // RuntimeException, not MessagingException) so that email failures never fail the
            // registration/create flow.
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return RegisterResponse.builder()
                .messageResponse("User registered successfully")
                .token(token)
                .user(convertToDTO(user))
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserException("Invalid credentials");
        }

        // Create a UserDetails instance that returns email as username
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );

        Map<String, String> extraClaims = Map.of(
                "role", user.getRole().toString(),
                "username", user.getUsername()
        );

        // Now generate the token
        String token = jwtService.generateToken(extraClaims, userDetails, 1000 * 60 * 60);

        return AuthenticationResponse.builder()
                .token(token)
                .messageResponse("Authentication successful")
                .user(convertToDTO(user))
                .build();
    }


    @Override
    public UserDTO update(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException("User not found"));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPosition(userDTO.getPosition());
        user.setDepartment(userDTO.getDepartment());
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserException("User not found");
        }

        // 1. Remove user from chat_message_read_by join table
        chatMessageRepository.removeFromReadBy(id);
        // 2. Delete all chat messages sent/received by this user
        chatMessageRepository.deleteBySenderOrRecipient(id);
        // 3. Delete all notifications received by this user
        notificationRepository.deleteByRecipientId(id);
        // 4. Nullify approvedBy references in leave requests
        leaveRequestRepository.nullifyApprovedBy(id);
        // 5. Delete leave requests owned by this user
        leaveRequestRepository.deleteByUserId(id);
        // 6. Nullify approvedBy references in timesheets
        timeSheetRepository.nullifyApprovedBy(id);
        // 7. Delete timesheets owned by this user
        timeSheetRepository.deleteByUserId(id);
        // 8. Delete timesheet schedules for this user
        timesheetScheduleRepository.deleteByUserId(id);
        // 9. Remove from all meeting attendee lists
        meetingRepository.removeFromAttendees(id);
        // 10. Nullify organizer on meetings they organised (meeting remains but organizer = null)
        meetingRepository.nullifyOrganizer(id);
        // 11. Nullify author on task comments (comment stays, author shows as deleted)
        taskCommentRepository.nullifyAuthor(id);
        // 12. Nullify assigned_to and created_by on tasks
        taskRepository.nullifyAssignedTo(id);
        taskRepository.nullifyCreatedBy(id);
        // 13. Nullify evaluator on performance evaluations
        performanceEvaluationRepository.nullifyEvaluator(id);
        // 14. Delete performance evaluations where this user is the subject
        performanceEvaluationRepository.deleteByUserId(id);
        // 15. Remove from team memberships
        teamMemberRepository.deleteByUserId(id);
        // 16. Nullify report generator references
        reportRepository.nullifyGeneratedBy(id);
        // 17. Nullify team leader references
        teamRepository.nullifyTeamLeader(id);
        // 18. Nullify project createdBy references
        projectRepository.nullifyCreatedBy(id);
        // 19. Finally delete the user
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("User not found"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .position(user.getPosition())
                .department(user.getDepartment())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .usedDaysThisYear(user.getUsedDaysThisYear())
                .leaveBalance(30 - user.getUsedDaysThisYear())
                .build();
    }
}
