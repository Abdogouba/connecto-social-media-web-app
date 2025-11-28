package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // Get all followings for a specific user, ordered by newest first
    Page<Follow> findAllByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    // Get all followers if a specific user, ordered by newest first
    Page<Follow> findAllByFollowedIdOrderByCreatedAtDesc(Long followedId, Pageable pageable);

}
