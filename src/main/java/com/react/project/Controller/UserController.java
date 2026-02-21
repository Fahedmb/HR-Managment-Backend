package com.react.project.Controller;

import com.react.project.DTO.UserDTO;
import com.react.project.Enumirator.Role;
import com.react.project.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HR & user management endpoints.
 * HR-only actions require ROLE_HR.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Any authenticated user can fetch themselves or others */
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.findById(id);
    }

    /** Get all users – HR only */
    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public List<UserDTO> getAllUsers() {
        return userService.findAll();
    }

    /** Filter by department */
    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('HR')")
    public List<UserDTO> getByDepartment(@PathVariable String department) {
        return userService.findByDepartment(department);
    }

    /** Filter by role */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('HR')")
    public List<UserDTO> getByRole(@PathVariable Role role) {
        return userService.findByRole(role);
    }

    /** Update own profile or HR updates any profile */
    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.update(id, userDTO);
    }

    /** HR can change a user's role */
    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('HR')")
    public UserDTO changeRole(@PathVariable Long id, @RequestBody UserDTO body) {
        return userService.changeRole(id, body.getRole());
    }

    /** HR resets a user's password */
    @PatchMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('HR')")
    public void resetPassword(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        userService.resetPassword(id, body.get("newPassword"));
    }

    /** HR deletes a user */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('HR')")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}


