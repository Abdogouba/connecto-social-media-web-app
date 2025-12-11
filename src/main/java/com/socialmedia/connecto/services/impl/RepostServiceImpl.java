package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.dtos.RepostResponseDTO;
import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.RepostRepository;
import com.socialmedia.connecto.services.*;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.NoSuchElementException;

@Service
public class RepostServiceImpl implements RepostService {

    private final RepostRepository repostRepository;
    private final UserService userService;
    private final PostService postService;
    private final NotificationService notificationService;
    private final BlockService blockService;

    public RepostServiceImpl(RepostRepository repostRepository, UserService userService, PostService postService, NotificationService notificationService, BlockService blockService) {
        this.repostRepository = repostRepository;
        this.userService = userService;
        this.postService = postService;
        this.notificationService = notificationService;
        this.blockService = blockService;
    }

    @Override
    public RepostResponseDTO repost(Long postId) throws AccessDeniedException {
        User user = userService.getCurrentUser();

        Post post = postService.getPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found"));

        boolean isRepostingMyPost = user.getId().equals(post.getUser().getId());

        if (!isRepostingMyPost) {
            if (post.getUser().isPrivate())
                throw new AccessDeniedException("You cannot repost a post that belongs to a private user");

            boolean currentBlocksPoster = blockService.isBlocked(user.getId(), post.getUser().getId());
            boolean posterBlocksCurrent = blockService.isBlocked(post.getUser().getId(), user.getId());

            if (currentBlocksPoster)
                throw new AccessDeniedException("You cannot repost this post, you blocked post owner");

            if (posterBlocksCurrent)
                throw new AccessDeniedException("You cannot repost this post, post owner blocked you");
        }

        Repost repost = new Repost();
        repost.setReposter(user);
        repost.setPost(post);
        repost = repostRepository.save(repost);

        if (!isRepostingMyPost) {
            Notification notification = new Notification();
            notification.setReceiver(post.getUser());
            notification.setSender(user);
            notification.setType(NotificationType.SHARED_POST);
            notification.setReferenceId(postId);
            notificationService.saveNotification(notification);
        }

        RepostResponseDTO dto = new RepostResponseDTO();
        dto.setId(repost.getId());
        dto.setReposterId(user.getId());
        dto.setReposterName(user.getName());
        dto.setPostId(postId);
        dto.setCreatedAt(repost.getCreatedAt());

        return dto;
    }

}
