package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.BlockedUserDTO;
import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.FollowSuggestionUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
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

    @GetMapping("/{id}/following")
    public ResponseEntity<PagedDTO<FollowListUserDTO>> getFollowing(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws AccessDeniedException {
        PagedDTO<FollowListUserDTO> response = followService.getFollowing(id, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<PagedDTO<FollowListUserDTO>> getFollowers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws AccessDeniedException {
        PagedDTO<FollowListUserDTO> response = followService.getFollowers(id, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<PagedDTO<FollowSuggestionUserDTO>> getFollowSuggestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedDTO<FollowSuggestionUserDTO> response = followService.getFollowSuggestions(page, size);
        return ResponseEntity.ok(response);
    }

}
