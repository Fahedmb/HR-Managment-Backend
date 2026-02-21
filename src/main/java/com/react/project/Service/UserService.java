package com.react.project.Service;

import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.DTO.UserDTO;
import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;
import com.react.project.Enumirator.Role;
import com.react.project.Model.User;

import java.util.List;

public interface UserService {
    UserDTO findById(Long id);
    List<UserDTO> findAll();
    List<UserDTO> findByDepartment(String department);
    List<UserDTO> findByRole(Role role);
    RegisterResponse register(RegisterRequest request);
    UserDTO update(Long id, UserDTO userDTO);
    UserDTO changeRole(Long id, Role newRole);
    void resetPassword(Long id, String newPassword);
    void delete(Long id);
    UserDTO getUserByEmail(String email);
    User getUserEntityById(Long userId);
    UserDTO getUserById(Long userId);
    void incrementUsedLeaveDays(Long userId, int days);
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
