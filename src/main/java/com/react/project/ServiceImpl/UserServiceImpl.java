package com.react.project.ServiceImpl;

import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;
import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.Enumirator.Role;
import com.react.project.Exception.UserException;
import com.react.project.Model.User;
import com.react.project.Repository.UserRepository;
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
        } catch (MessagingException e) {
            // Log but don't fail registration
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
    public void delete(Long id) {
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
                .build();
    }
}
