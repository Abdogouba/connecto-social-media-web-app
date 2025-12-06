package com.socialmedia.connecto.controllers;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.FollowRequestAction;
import com.socialmedia.connecto.dtos.FollowRequestResponseDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.services.FollowRequestService;
import jakarta.validation.Valid;
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

    @GetMapping("/sent")
    public ResponseEntity<PagedDTO<FollowListUserDTO>> getFollowRequestsSent(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedDTO<FollowListUserDTO> response = followRequestService.getFollowRequestsSent(page, size);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/sent/{id}")
    public ResponseEntity cancelFollowRequestSent(@PathVariable Long id) {
        followRequestService.cancelFollowRequestSent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/respond")
    public ResponseEntity<String> respondToFollowRequest(@PathVariable Long id, @Valid @RequestBody FollowRequestResponseDTO dto) {
        String response = followRequestService.respondToFollowRequest(id, dto);
        return ResponseEntity.ok(response);
    }

}
