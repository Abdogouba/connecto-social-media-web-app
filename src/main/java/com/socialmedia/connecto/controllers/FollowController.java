package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.services.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> follow(@PathVariable Long id) throws AccessDeniedException {
        return ResponseEntity.ok(followService.follow(id));
    }

}
