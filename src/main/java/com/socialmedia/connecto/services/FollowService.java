package com.socialmedia.connecto.services;

import com.socialmedia.connecto.dtos.FollowListUserDTO;
import com.socialmedia.connecto.dtos.PagedDTO;
import com.socialmedia.connecto.models.User;

import java.nio.file.AccessDeniedException;

public interface FollowService {

    void removeFollowRelationshipsIfExists(User current, User target);

    String follow(Long id) throws AccessDeniedException;

    void unfollow(Long id);

    void removeFollower(Long id) throws AccessDeniedException;

    PagedDTO<FollowListUserDTO> getFollowing(Long id, int page, int size) throws AccessDeniedException;

    PagedDTO<FollowListUserDTO> getFollowers(int page, int size);

}
