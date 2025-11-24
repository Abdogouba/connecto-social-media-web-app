package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.FollowRequest;
import com.socialmedia.connecto.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

}
