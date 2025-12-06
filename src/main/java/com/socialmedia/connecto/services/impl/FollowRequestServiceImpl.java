package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.FollowRequestAction;
import com.socialmedia.connecto.dtos.FollowRequestResponseDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.FollowRepository;
import com.socialmedia.connecto.repositories.FollowRequestRepository;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.NotificationService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FollowRequestServiceImpl implements FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;

    public FollowRequestServiceImpl(FollowRequestRepository followRequestRepository, UserService userService, FollowRepository followRepository, NotificationService notificationService) {
        this.followRequestRepository = followRequestRepository;
        this.userService = userService;
        this.followRepository = followRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void removeFollowRequestRelationshipsIfExists(User current, User target) {
        followRequestRepository.deleteByFollowerIdAndFollowedId(current.getId(), target.getId());
        followRequestRepository.deleteByFollowerIdAndFollowedId(target.getId(), current.getId());
    }

    @Override
    public boolean existsByFollowerAndFollowed(User follower, User followed) {
        return followRequestRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId());
    }

    @Override
    public void createAndSave(User follower, User followed) {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setFollower(follower);
        followRequest.setFollowed(followed);
        followRequestRepository.save(followRequest);
    }

    @Override
    public PagedDTO<FollowListUserDTO> getFollowRequestsReceived(int page, int size) {
        User user = userService.getCurrentUser();

        if (!user.isPrivate())
            throw new IllegalArgumentException("Public users do not have follow requests");

        Pageable pageable = PageRequest.of(page, size);

        Page<FollowRequest> followRequestPage = followRequestRepository.findAllByFollowedIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<FollowListUserDTO> dtos = followRequestPage.getContent().stream().map(f -> {
            FollowListUserDTO dto = new FollowListUserDTO();
            dto.setId(f.getFollower().getId());
            dto.setName(f.getFollower().getName());
            dto.setFollowedAt(f.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<FollowListUserDTO>(
                dtos,
                followRequestPage.getNumber(),
                followRequestPage.getTotalPages(),
                followRequestPage.getTotalElements()
        );
    }

    @Override
    public PagedDTO<FollowListUserDTO> getFollowRequestsSent(int page, int size) {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<FollowRequest> followRequestPage = followRequestRepository.findAllByFollowerIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<FollowListUserDTO> dtos = followRequestPage.getContent().stream().map(f -> {
            FollowListUserDTO dto = new FollowListUserDTO();
            dto.setId(f.getFollowed().getId());
            dto.setName(f.getFollowed().getName());
            dto.setFollowedAt(f.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<FollowListUserDTO>(
                dtos,
                followRequestPage.getNumber(),
                followRequestPage.getTotalPages(),
                followRequestPage.getTotalElements()
        );
    }

    @Override
    @Transactional
    public void cancelFollowRequestSent(Long followedId) {
        User currentUser = userService.getCurrentUser();

        if (followedId.equals(currentUser.getId()))
            throw new IllegalArgumentException("Target id cannot be equal to current user id");

        followRequestRepository.deleteByFollowerIdAndFollowedId(currentUser.getId(), followedId);
    }

    @Override
    @Transactional
    public String respondToFollowRequest(Long followerId, FollowRequestResponseDTO dto) {
        User currentUser = userService.getCurrentUser();

        FollowRequestAction action = dto.getAction();

        if (!currentUser.isPrivate())
            throw new IllegalStateException("Public users do not have follow requests");

        if (followerId.equals(currentUser.getId()))
            throw new IllegalArgumentException("Target id is invalid");

        User follower = userService.getUserById(followerId)
                .orElseThrow(() -> new NoSuchElementException("Follow request sender not found"));

        if (!followRequestRepository.existsByFollowerIdAndFollowedId(followerId, currentUser.getId()))
            throw new NoSuchElementException("Follow request not found");

        followRequestRepository.deleteByFollowerIdAndFollowedId(followerId, currentUser.getId());

        String response = "";

        if (action == FollowRequestAction.ACCEPT) {
            response = "Follow request accepted";

            if (followRepository.existsByFollowerIdAndFollowedId(followerId, currentUser.getId()))
                return "Target user already follows current user";

            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowed(currentUser);
            followRepository.save(follow);

            Notification notification = new Notification();
            notification.setReceiver(follower);
            notification.setSender(currentUser);
            notification.setType(NotificationType.FOLLOW_ACCEPTED);
            notificationService.saveNotification(notification);
        } else {
            response = "Follow request rejected";
        }

        return response;
    }

}
