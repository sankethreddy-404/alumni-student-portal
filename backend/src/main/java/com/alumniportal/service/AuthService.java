package com.alumniportal.service;

import com.alumniportal.config.JwtUtil;
import com.alumniportal.dto.AuthResponse;
import com.alumniportal.dto.LoginRequest;
import com.alumniportal.dto.RegisterRequest;
import com.alumniportal.entity.*;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.AlumniProfileRepository;
import com.alumniportal.repository.StudentProfileRepository;
import com.alumniportal.repository.UserRepository;
import com.alumniportal.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AlumniProfileRepository alumniProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new ApiException("Admin accounts cannot be self-registered", HttpStatus.FORBIDDEN);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("An account with this email already exists", HttpStatus.CONFLICT);
        }

        // Alumni accounts require admin approval before login; students are approved immediately
        boolean approved = request.getRole() != Role.ALUMNI;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .approved(approved)
                .active(true)
                .build();
        user = userRepository.save(user);

        if (request.getRole() == Role.ALUMNI) {
            AlumniProfile profile = AlumniProfile.builder().user(user).build();
            alumniProfileRepository.save(profile);
        } else if (request.getRole() == Role.STUDENT) {
            StudentProfile profile = StudentProfile.builder().user(user).build();
            studentProfileRepository.save(profile);
        }

        if (!approved) {
            return new AuthResponse(null, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
        }

        String token = jwtUtil.generateToken(new CustomUserDetails(user), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new ApiException("This account has been deactivated", HttpStatus.FORBIDDEN);
        }
        if (!user.isApproved()) {
            throw new ApiException("Your alumni account is pending admin approval", HttpStatus.FORBIDDEN);
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(new CustomUserDetails(user), user.getId(), user.getRole().name());
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
