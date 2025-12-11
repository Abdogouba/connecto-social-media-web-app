package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.RepostResponseDTO;

import java.nio.file.AccessDeniedException;

public interface RepostService {

    RepostResponseDTO repost(Long postId) throws AccessDeniedException;

}
