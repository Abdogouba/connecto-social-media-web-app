package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.PostRequestDTO;
import com.socialmedia.connecto.dtos.PostResponseDTO;

import java.nio.file.AccessDeniedException;

public interface PostService {

    PostResponseDTO createPost(PostRequestDTO dto);

    PostResponseDTO updatePost(Long postId, PostRequestDTO dto) throws AccessDeniedException;

}
