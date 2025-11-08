package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.ChangePasswordDTO;
import com.socialmedia.connecto.dtos.CreatePostRequestDTO;
import com.socialmedia.connecto.dtos.CreatePostResponseDTO;
import com.socialmedia.connecto.dtos.EditProfileDTO;

public interface UserService {

    void editProfile(EditProfileDTO dto);

    void changePassword(ChangePasswordDTO dto);

}
