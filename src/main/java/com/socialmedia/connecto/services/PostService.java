package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.dtos.PostRequestDTO;
import com.socialmedia.connecto.dtos.PostResponseDTO;
import com.socialmedia.connecto.dtos.ReposterDTO;
import com.socialmedia.connecto.models.Post;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

public interface PostService {

    PostResponseDTO createPost(PostRequestDTO dto);

    PostResponseDTO updatePost(Long postId, PostRequestDTO dto) throws AccessDeniedException;

    Optional<Post> getPostById(Long postId);

    PagedDTO<ReposterDTO> getReposters(Long postId, int page, int size) throws AccessDeniedException;

    void savePost(Long postId) throws AccessDeniedException;

    void unsavePost(Long postId);

}
