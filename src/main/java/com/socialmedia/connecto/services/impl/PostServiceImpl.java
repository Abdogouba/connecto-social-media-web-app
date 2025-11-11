package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.PostRequestDTO;
import com.socialmedia.connecto.dtos.PostResponseDTO;
import com.socialmedia.connecto.models.Post;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.PostRepository;
import com.socialmedia.connecto.repositories.UserRepository;
import com.socialmedia.connecto.services.PostService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserService userService;

    public PostServiceImpl(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Override
    public PostResponseDTO createPost(PostRequestDTO dto) {
        Post post = new Post();
        post.setContent(dto.getContent());
        post.setUser(userService.getUser());

        Post savedPost = postRepository.save(post);

        return mapPostToResponseDTO(savedPost);
    }

    @Override
    @Transactional
    public PostResponseDTO updatePost(Long postId, PostRequestDTO dto) throws AccessDeniedException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        User user = userService.getUser();

        if (!post.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("You can only edit your posts");

        if (post.getContent().equals(dto.getContent())) {
            return mapPostToResponseDTO(post);
        }

        post.setContent(dto.getContent());

        Post savedPost = postRepository.save(post);

        return mapPostToResponseDTO(savedPost);
    }

    private static PostResponseDTO mapPostToResponseDTO(Post savedPost) {
        PostResponseDTO responseDTO = new PostResponseDTO();
        responseDTO.setId(savedPost.getId());
        responseDTO.setContent(savedPost.getContent());
        responseDTO.setUserId(savedPost.getUser().getId());
        responseDTO.setUserName(savedPost.getUser().getName());
        responseDTO.setCreatedAt(savedPost.getCreatedAt());
        return responseDTO;
    }

}
