package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.CreatePostRequestDTO;
import com.socialmedia.connecto.dtos.CreatePostResponseDTO;

public interface PostService {

    CreatePostResponseDTO createPost(CreatePostRequestDTO dto);

}
