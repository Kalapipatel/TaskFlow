package com.TaskFlow.UserService.controller;

import com.TaskFlow.UserService.dto.LoginRequest;
import com.TaskFlow.UserService.dto.LoginRespose;
import com.TaskFlow.UserService.dto.SignupRequest;
import com.TaskFlow.UserService.dto.SignupResponse;
import com.TaskFlow.UserService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest){
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginRespose> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }
}
