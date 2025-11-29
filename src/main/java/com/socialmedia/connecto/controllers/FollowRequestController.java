package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.services.FollowRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/follow-requests")
public class FollowRequestController {

    private final FollowRequestService followRequestService;

    public FollowRequestController(FollowRequestService followRequestService) {
        this.followRequestService = followRequestService;
    }

    @GetMapping("/received")
    public ResponseEntity<PagedDTO<FollowListUserDTO>> getFollowRequestsReceived(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedDTO<FollowListUserDTO> response = followRequestService.getFollowRequestsReceived(page, size);
        return ResponseEntity.ok(response);
    }

}
