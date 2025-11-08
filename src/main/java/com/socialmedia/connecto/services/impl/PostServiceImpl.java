package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.CreatePostRequestDTO;
import com.socialmedia.connecto.dtos.CreatePostResponseDTO;
import com.socialmedia.connecto.models.Post;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.PostRepository;
import com.socialmedia.connecto.repositories.UserRepository;
import com.socialmedia.connecto.services.PostService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostServiceImpl(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    private User getUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }

    @Override
    public CreatePostResponseDTO createPost(CreatePostRequestDTO dto) {
        Post post = new Post();
        post.setContent(dto.getContent());
        post.setUser(getUser());

        Post savedPost = postRepository.save(post);

        CreatePostResponseDTO responseDTO = new CreatePostResponseDTO();
        responseDTO.setId(savedPost.getId());
        responseDTO.setContent(savedPost.getContent());
        responseDTO.setUserId(savedPost.getUser().getId());
        responseDTO.setUserName(savedPost.getUser().getName());
        responseDTO.setCreatedAt(savedPost.getCreatedAt());

        return responseDTO;
    }

}
