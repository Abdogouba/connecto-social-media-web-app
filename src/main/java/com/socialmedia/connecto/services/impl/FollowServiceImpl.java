package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.BlockedUserDTO;
import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.BlockRepository;
import com.socialmedia.connecto.repositories.FollowRepository;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.FollowService;
import com.socialmedia.connecto.services.NotificationService;
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
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;
    private final BlockRepository blockRepository;
    private final FollowRequestService followRequestService;
    private final NotificationService notificationService;

    public FollowServiceImpl(FollowRepository followRepository, UserService userService, BlockRepository blockRepository, FollowRequestService followRequestService, NotificationService notificationService) {
        this.followRepository = followRepository;
        this.userService = userService;
        this.blockRepository = blockRepository;
        this.followRequestService = followRequestService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void removeFollowRelationshipsIfExists(User current, User target) {
        followRepository.deleteByFollowerIdAndFollowedId(target.getId(), current.getId());
        followRepository.deleteByFollowerIdAndFollowedId(current.getId(), target.getId());
    }

    @Override
    @Transactional
    public String follow(Long id) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getId().equals(id))
            throw new IllegalArgumentException("User cannot follow himself");

        User target = userService.getUserById(id)
                .orElseThrow(() -> new NoSuchElementException("User to be followed not found"));

        boolean currentBlocksTarget = blockRepository.existsByBlockerIdAndBlockedId(currentUser.getId(), target.getId());
        boolean targetBlocksCurrent = blockRepository.existsByBlockerIdAndBlockedId(target.getId(), currentUser.getId());

        if (currentBlocksTarget)
            throw new IllegalStateException("User cannot follow a user he blocked");

        if (targetBlocksCurrent)
            throw new AccessDeniedException("User cannot follow a user that blocked him");

        if (followRepository.existsByFollowerIdAndFollowedId(currentUser.getId(), id))
            throw new IllegalStateException("User already follows this user");

        NotificationType notificationType;
        String response;

        if (target.isPrivate()) {
            if (followRequestService.existsByFollowerAndFollowed(currentUser, target))
                throw new IllegalStateException("User already sent a follow request");
            followRequestService.createAndSave(currentUser, target);
            notificationType = NotificationType.FOLLOW_REQUEST;
            response = "Follow request sent";
        } else {
            createAndSave(currentUser, target);
            notificationType = NotificationType.NEW_FOLLOWER;
            response = "Follow was successful";
        }

        Notification notification = new Notification();
        notification.setReceiver(target);
        notification.setSender(currentUser);
        notification.setType(notificationType);
        notificationService.saveNotification(notification);

        return response;
    }

    @Override
    @Transactional
    public void unfollow(Long id) {
        User currentUser = userService.getCurrentUser();

        if (currentUser.getId().equals(id))
            throw new IllegalArgumentException("User cannot unfollow himself");

        User target = userService.getUserById(id)
                .orElseThrow(() -> new NoSuchElementException("User to be unfollowed not found"));

        followRepository.deleteByFollowerIdAndFollowedId(currentUser.getId(), target.getId());
    }

    @Override
    @Transactional
    public void removeFollower(Long id) throws AccessDeniedException {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.isPrivate())
            throw new AccessDeniedException("Public users cannot remove a follower");

        if (currentUser.getId().equals(id))
            throw new IllegalArgumentException("User cannot remove himself from followers");

        User target = userService.getUserById(id)
                .orElseThrow(() -> new NoSuchElementException("User to be removed from followers not found"));

        followRepository.deleteByFollowerIdAndFollowedId(target.getId(), currentUser.getId());
    }

    @Override
    public PagedDTO<FollowListUserDTO> getFollowing(Long id, int page, int size) throws AccessDeniedException {
        User user = userService.getCurrentUser();

        User target = userService.getUserById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        boolean currentBlocksTarget = blockRepository.existsByBlockerIdAndBlockedId(user.getId(), target.getId());
        boolean targetBlocksCurrent = blockRepository.existsByBlockerIdAndBlockedId(target.getId(), user.getId());

        if (currentBlocksTarget)
            throw new IllegalStateException("User cannot view following list of a user he blocked");

        if (targetBlocksCurrent)
            throw new AccessDeniedException("User cannot view following list of a user that blocked him");

        if (!target.getId().equals(user.getId()) && target.isPrivate() && !followRepository.existsByFollowerIdAndFollowedId(user.getId(), id))
            throw new AccessDeniedException("User cannot view following list of a private user he is not following");

        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followPage = followRepository.findAllByFollowerIdOrderByCreatedAtDesc(id, pageable);

        List<FollowListUserDTO> dtos = followPage.getContent().stream().map(f -> {
            FollowListUserDTO dto = new FollowListUserDTO();
            dto.setId(f.getFollowed().getId());
            dto.setName(f.getFollowed().getName());
            dto.setFollowedAt(f.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<FollowListUserDTO>(
                dtos,
                followPage.getNumber(),
                followPage.getTotalPages(),
                followPage.getTotalElements()
        );
    }

    @Override
    public PagedDTO<FollowListUserDTO> getFollowers(int page, int size) {
        User user = userService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followPage = followRepository.findAllByFollowedIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<FollowListUserDTO> dtos = followPage.getContent().stream().map(f -> {
            FollowListUserDTO dto = new FollowListUserDTO();
            dto.setId(f.getFollower().getId());
            dto.setName(f.getFollower().getName());
            dto.setFollowedAt(f.getCreatedAt());
            return dto;
        }).toList();

        return new PagedDTO<FollowListUserDTO>(
                dtos,
                followPage.getNumber(),
                followPage.getTotalPages(),
                followPage.getTotalElements()
        );
    }

    public void createAndSave(User currentUser, User target) {
        Follow follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowed(target);
        followRepository.save(follow);
    }


}
