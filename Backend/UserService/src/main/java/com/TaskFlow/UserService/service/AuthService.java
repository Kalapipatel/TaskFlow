package com.TaskFlow.UserService.service;

import com.TaskFlow.UserService.dto.LoginRequest;
import com.TaskFlow.UserService.dto.LoginResponse;
import com.TaskFlow.UserService.dto.SignupRequest;
import com.TaskFlow.UserService.dto.SignupResponse;
import com.TaskFlow.UserService.entity.User;
import com.TaskFlow.UserService.repository.UserRepository;
import com.TaskFlow.UserService.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtUtils.generateToken(user);

        return new LoginResponse(user.getUsername(), token);
    }

    public SignupResponse signup(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. Instantiate a NEW user (fixes the NullPointerException)
        User user = new User();
        user.setEmail(signupRequest.getEmail());

        // 3. Hash the password before saving!
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));

        user.setUsername(signupRequest.getUsername());
        user.setFullName(signupRequest.getFullname());

        user = userRepository.save(user);

        return new SignupResponse(user.getId(), user.getUsername());
    }


}
