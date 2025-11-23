package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByFollowerAndFollowed(User follower, User followed);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

}
