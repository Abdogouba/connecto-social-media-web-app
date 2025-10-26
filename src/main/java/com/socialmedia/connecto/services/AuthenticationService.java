package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.LoginRequestDTO;
import com.socialmedia.connecto.dtos.LoginResponseDTO;
import com.socialmedia.connecto.dtos.UserRegistrationDTO;

public interface AuthenticationService {

    void register(UserRegistrationDTO dto);

    LoginResponseDTO login(LoginRequestDTO dto);

}
