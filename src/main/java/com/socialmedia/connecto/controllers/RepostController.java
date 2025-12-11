package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.RepostResponseDTO;
import com.socialmedia.connecto.services.RepostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/reposts")
public class RepostController {

    private final RepostService repostService;

    public RepostController(RepostService repostService) {
        this.repostService = repostService;
    }

    @PostMapping("/{postId}")
    public ResponseEntity<RepostResponseDTO> repost(@PathVariable Long postId) throws AccessDeniedException {
        RepostResponseDTO dto = repostService.repost(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

}
