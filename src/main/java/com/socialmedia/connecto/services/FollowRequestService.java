package com.socialmedia.connecto.services;

import com.socialmedia.connecto.models.User;

public interface FollowRequestService {

    void removeFollowRequestRelationshipsIfExists(User current, User target);

}
