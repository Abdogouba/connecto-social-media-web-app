package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.UserRegistrationDTO;

public interface AuthenticationService {
    void register(UserRegistrationDTO dto);
}
