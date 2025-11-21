package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.ChangePasswordDTO;
import com.socialmedia.connecto.dtos.EditProfileDTO;
import com.socialmedia.connecto.models.User;

import java.util.Optional;

public interface UserService {

    void editProfile(EditProfileDTO dto);

    void changePassword(ChangePasswordDTO dto);

    User getCurrentUser();

    Optional<User> getUserById(Long id);
}
