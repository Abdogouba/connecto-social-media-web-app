package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.FollowRepository;
import com.socialmedia.connecto.services.FollowService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    public FollowServiceImpl(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    @Override
    @Transactional
    public void removeFollowRelationshipsIfExists(User current, User target) {
        followRepository.deleteByFollowerAndFollowed(target, current);
        followRepository.deleteByFollowerAndFollowed(current, target);
    }

}
