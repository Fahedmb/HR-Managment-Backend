package com.react.project.Controller;

import com.react.project.DTO.RegisterRequest;
import com.react.project.DTO.RegisterResponse;
import com.react.project.dto.UserDTO;
import com.react.project.DTO.AuthenticationRequest;
import com.react.project.DTO.AuthenticationResponse;
import com.react.project.Exception.UserException;
import com.react.project.Service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        RegisterResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Void> authenticate(@RequestBody AuthenticationRequest request, HttpServletResponse response) {
        // Authenticate the user and retrieve the JWT token
        AuthenticationResponse authResponse = userService.authenticate(request);

        // Create the JWT cookie (without SameSite)
        Cookie jwtCookie = new Cookie("jwt", authResponse.getToken());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);  // Use secure cookies for HTTPS only
        jwtCookie.setPath("/");     // Cookie is accessible for the entire application
        jwtCookie.setMaxAge(60 * 60); // 1 hour expiration time

        // Optional: Set domain explicitly if necessary (e.g., for subdomains)
        // jwtCookie.setDomain("localhost"); // Set domain if needed

        // Add the JWT cookie to the response
        response.addCookie(jwtCookie);

        // Manually set the SameSite attribute using the Set-Cookie header
        String cookieValue = "jwt=" + authResponse.getToken() +
                "; HttpOnly; Secure; Path=/; Max-Age=3600; SameSite=None";
        response.setHeader("Set-Cookie", cookieValue);

        // For debugging purposes, log the Set-Cookie header to verify the correct values
        System.out.println("Set-Cookie header: " + cookieValue);

        // Return a successful response
        return ResponseEntity.ok().build();
    }




    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        response.addCookie(jwtCookie);

        return ResponseEntity.ok().build();
    }
}


//    // Authenticate a user
//    @PostMapping("/authenticate")
//    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request, BindingResult result) {
//        if (result.hasErrors()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(AuthenticationResponse.builder()
//                            .messageResponse("Validation failed, Auth error")
//                            .build());
//        }
//        try {
//            AuthenticationResponse response = userService.authenticate(request);
//            return ResponseEntity.ok(response);
//        } catch (UserException e) {
//            log.error("Authentication error: {}", e.getMessage());
//            if ("Bad credentials".equals(e.getMessage())) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(AuthenticationResponse.builder()
//                                .messageResponse("User not found")
//                                .build());
//            } else if ("User is disabled".equals(e.getMessage())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body(AuthenticationResponse.builder()
//                                .messageResponse("User account is not active. Please confirm your email.")
//                                .build());
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(AuthenticationResponse.builder()
//                                .messageResponse("An error occurred during authentication.")
//                                .build());
//            }
//        }
//    }

//    // Forgot password - sends a reset token to the user's email
//    @PostMapping("/forgot-password")
//    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> payload) {
//        String email = payload.get("email");
//        userService.forgotPassword(email);
//        return ResponseEntity.ok().build();
//    }
//
//    // Reset password - accepts token and new password to change the user's password
//    @PostMapping("/reset-password")
//    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> payload) {
//        long token = Long.parseLong(payload.get("token"));
//        String newPassword = payload.get("newPassword");
//        userService.resetPassword(token, newPassword);
//        return ResponseEntity.ok().build();
//    }
//
//    // Resend reset password token if the user didn't receive it
//    @PostMapping("/resend-reset-password")
//    public ResponseEntity<Void> resendResetPasswordToken(@RequestBody Map<String, String> payload) {
//        String email = payload.get("email");
//        userService.resendResetPasswordToken(email);
//        return ResponseEntity.ok().build();
//    }

