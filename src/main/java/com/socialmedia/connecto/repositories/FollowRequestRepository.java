package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.FollowRequest;
import com.socialmedia.connecto.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // Get all follow requests received, ordered by newest first
    Page<FollowRequest> findAllByFollowedIdOrderByCreatedAtDesc(Long followedId, Pageable pageable);

    // Get all follow requests sent, ordered by newest first
    Page<FollowRequest> findAllByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

}
