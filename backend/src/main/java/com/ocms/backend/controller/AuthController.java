package com.ocms.backend.controller;

import com.ocms.backend.dto.AuthResponse;
import com.ocms.backend.dto.LoginRequest;
import com.ocms.backend.dto.RegisterRequest;
import com.ocms.backend.model.User;
import com.ocms.backend.repository.UserRepository;
import com.ocms.backend.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        User saved = userRepository.save(user);
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(saved.getEmail(), saved.getPassword(), java.util.List.of())
        );
        return ResponseEntity.ok(new AuthResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtService.generateToken(
                new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), java.util.List.of())
        );
        return ResponseEntity.ok(new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole()));
    }
}
