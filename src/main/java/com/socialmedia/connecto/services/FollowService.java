package com.socialmedia.connecto.services;

import com.socialmedia.connecto.models.User;

public interface FollowService {

    void removeFollowRelationshipsIfExists(User current, User target);

}
