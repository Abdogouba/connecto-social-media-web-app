package com.socialmedia.connecto.services.impl;

import com.socialmedia.connecto.models.FollowRequest;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.FollowRequestRepository;
import com.socialmedia.connecto.services.FollowRequestService;
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
}
