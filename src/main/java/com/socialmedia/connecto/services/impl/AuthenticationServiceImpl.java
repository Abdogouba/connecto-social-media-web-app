package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.auth.JwtUtil;
import com.socialmedia.connecto.dtos.LoginRequestDTO;
import com.socialmedia.connecto.dtos.LoginResponseDTO;
import com.socialmedia.connecto.dtos.UserRegistrationDTO;
import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Role;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.UserRepository;
import com.socialmedia.connecto.services.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void register(UserRegistrationDTO dto) {
        if (this.userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already registered");

        User user = new User();
        user.setId(null);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setBirthDate(dto.getBirthDate());
        user.setLocation(dto.getLocation());
        user.setBio(dto.getBio());
        user.setPrivate(false);
        user.setBanned(false);
        user.setRole(Role.USER);
        user.setPictureURL(dto.getPictureURL());

        if (dto.getGender().equalsIgnoreCase("male"))
            user.setGender(Gender.MALE);
        else if (dto.getGender().equalsIgnoreCase("female"))
            user.setGender(Gender.FEMALE);
        else
            throw new RuntimeException("Invalid gender");

        this.userRepository.save(user);

    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.isBanned())
            throw new RuntimeException("User is currently banned from the platform");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new LoginResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getGender(),
                user.getBirthDate(),
                user.getLocation(),
                user.getBio(),
                user.isPrivate(),
                user.getRole(),
                user.getPictureURL(),
                user.getCreatedAt()
        );
    }

    @Override
    public void forgotPassword(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            String tempPassword = UUID.randomUUID().toString().substring(0, 8);
            try {
                this.emailService.sendPassword(user.get().getEmail(), tempPassword);
                user.get().setPassword(passwordEncoder.encode(tempPassword));
                userRepository.save(user.get());
            } catch (Exception e) {
                System.out.println("Failed to send email: " + e.getMessage());
            }
        }

    }

}
