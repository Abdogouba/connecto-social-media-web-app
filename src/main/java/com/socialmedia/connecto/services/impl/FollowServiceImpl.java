package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.Notification;
import com.socialmedia.connecto.models.NotificationType;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.BlockRepository;
import com.socialmedia.connecto.repositories.FollowRepository;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.FollowService;
import com.socialmedia.connecto.services.NotificationService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
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

    public void createAndSave(User currentUser, User target) {
        Follow follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowed(target);
        followRepository.save(follow);
    }


}
