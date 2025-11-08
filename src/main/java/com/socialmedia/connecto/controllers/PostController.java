package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.ChangePasswordDTO;
import com.socialmedia.connecto.dtos.CreatePostRequestDTO;
import com.socialmedia.connecto.dtos.CreatePostResponseDTO;
import com.socialmedia.connecto.dtos.EditProfileDTO;
import com.socialmedia.connecto.services.PostService;
import com.socialmedia.connecto.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<CreatePostResponseDTO> createPost(@Valid @RequestBody CreatePostRequestDTO dto) {
        CreatePostResponseDTO responseDTO = postService.createPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

}

