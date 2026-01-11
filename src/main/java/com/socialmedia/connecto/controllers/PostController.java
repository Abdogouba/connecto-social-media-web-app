package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.*;
import com.socialmedia.connecto.services.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostRequestDTO dto) {
        PostResponseDTO responseDTO = postService.createPost(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity updatePost(@PathVariable Long id, @Valid @RequestBody PostRequestDTO dto) throws AccessDeniedException {
        PostResponseDTO responseDTO = postService.updatePost(id, dto);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{id}/reposts")
    public ResponseEntity<PagedDTO<ReposterDTO>> getReposters(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws AccessDeniedException {
        PagedDTO<ReposterDTO> response = postService.getReposters(id, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<String> savePost(@PathVariable Long id) throws AccessDeniedException {
        postService.savePost(id);
        return ResponseEntity.status(HttpStatus.OK).body("Post saved successfully");
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity unsavePost(@PathVariable Long id) {
        postService.unsavePost(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity likePost(@PathVariable Long id) throws AccessDeniedException {
        postService.likePost(id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

}

