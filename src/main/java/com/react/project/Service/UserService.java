package com.react.project.Service;

import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.DTO.UserDTO;
import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;

import java.util.List;

public interface UserService {
    UserDTO findById(Long id);
    List<UserDTO> findAll();
    RegisterResponse register(RegisterRequest request);
    UserDTO update(Long id, UserDTO userDTO);
    void delete(Long id);
    UserDTO getUserByEmail(String email);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}
