package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.ChangePasswordDTO;
import com.socialmedia.connecto.dtos.EditProfileDTO;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.UserRepository;
import com.socialmedia.connecto.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void editProfile(EditProfileDTO dto) {
        User user = getCurrentUser();

        user.setName(dto.getName());
        user.setLocation(dto.getLocation());
        user.setBio(dto.getBio());
        user.setPrivate(dto.getIsPrivate());

        userRepository.save(user);
    }

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void changePassword(ChangePasswordDTO dto) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Current password is not correct");

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.save(user);
    }

}
