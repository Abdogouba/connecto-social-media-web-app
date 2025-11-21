package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.FollowRequestRepository;
import com.socialmedia.connecto.services.FollowRequestService;
import com.socialmedia.connecto.services.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowRequestServiceImpl implements FollowRequestService {

    private final FollowRequestRepository followRequestRepository;

    public FollowRequestServiceImpl(FollowRequestRepository followRequestRepository) {
        this.followRequestRepository = followRequestRepository;
    }

    @Override
    @Transactional
    public void removeFollowRequestRelationshipsIfExists(User current, User target) {
        followRequestRepository.deleteByFollowerAndFollowed(current, target);
        followRequestRepository.deleteByFollowerAndFollowed(target, current);
    }
}
