package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.services.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping("/{id}")
    public ResponseEntity unfollow(@PathVariable Long id) {
        followService.unfollow(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/followers/{id}")
    public ResponseEntity removeFollower(@PathVariable Long id) throws AccessDeniedException {
        followService.removeFollower(id);
        return ResponseEntity.noContent().build();
    }

}
