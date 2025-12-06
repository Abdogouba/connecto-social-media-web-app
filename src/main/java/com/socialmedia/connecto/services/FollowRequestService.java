package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.FollowRequestAction;
import com.socialmedia.connecto.dtos.FollowRequestResponseDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.User;

public interface FollowRequestService {

    void removeFollowRequestRelationshipsIfExists(User current, User target);

    boolean existsByFollowerAndFollowed(User follower, User followed);

    void createAndSave(User follower, User followed);

    PagedDTO<FollowListUserDTO> getFollowRequestsReceived(int page, int size);

    PagedDTO<FollowListUserDTO> getFollowRequestsSent(int page, int size);

    void cancelFollowRequestSent(Long followedId);

    String respondToFollowRequest(Long followerId, FollowRequestResponseDTO dto);

}
