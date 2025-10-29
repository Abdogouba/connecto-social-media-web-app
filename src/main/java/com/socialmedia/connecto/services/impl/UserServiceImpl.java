package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.EditProfileDTO;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.UserRepository;
import com.socialmedia.connecto.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void editProfile(EditProfileDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setLocation(dto.getLocation());
        user.setBio(dto.getBio());
        user.setPrivate(dto.getIsPrivate());
        user.setPictureURL(dto.getPictureURL());

        userRepository.save(user);
    }

}
