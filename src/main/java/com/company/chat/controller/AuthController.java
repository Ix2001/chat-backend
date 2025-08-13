package com.company.chat.controller;

import com.company.chat.model.User;
import com.company.chat.repository.UserRepository;
import com.company.chat.security.JwtService;
import com.company.chat.security.LoginRequest;
import com.company.chat.security.RegisterRequest;
import com.company.chat.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.status(409).body(Map.of("error","username already exists"));
        }
        userService.register(req);
        return ResponseEntity.ok(Map.of("status","ok"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        String token = jwt.generateToken(auth.getName(), Map.of());
        return ResponseEntity.ok(Map.of("token", token));
    }

}
