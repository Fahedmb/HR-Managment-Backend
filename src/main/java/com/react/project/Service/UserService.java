package com.react.project.Service;

import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.DTO.UserDTO;
import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;
import com.react.project.Model.User;

import java.util.List;

public interface UserService {
    UserDTO findById(Long id);
    List<UserDTO> findAll();
    RegisterResponse register(RegisterRequest request);
    UserDTO update(Long id, UserDTO userDTO);
    void delete(Long id);
    UserDTO getUserByEmail(String email);
    User getUserEntityById(Long userId);
    UserDTO getUserById(Long userId);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
