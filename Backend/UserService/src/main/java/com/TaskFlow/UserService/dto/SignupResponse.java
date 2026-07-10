package com.TaskFlow.UserService.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponse {
    private UUID id;
    private String username;
}
