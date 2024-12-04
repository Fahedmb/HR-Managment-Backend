package com.react.project.DTO;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private String messageResponse;
    private String user;
}
