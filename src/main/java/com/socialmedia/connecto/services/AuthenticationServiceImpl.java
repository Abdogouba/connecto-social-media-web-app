package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.UserRegistrationDTO;
import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Role;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
