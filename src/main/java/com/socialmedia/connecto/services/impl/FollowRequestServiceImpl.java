package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.FollowRequest;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.FollowRequestRepository;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class FollowRequestServiceImpl implements FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final UserService userService;

    public FollowRequestServiceImpl(FollowRequestRepository followRequestRepository, UserService userService) {
        this.followRequestRepository = followRequestRepository;
        this.userService = userService;
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

}
