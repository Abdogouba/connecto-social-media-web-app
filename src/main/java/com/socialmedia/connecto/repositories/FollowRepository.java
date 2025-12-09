package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.models.Follow;
import com.socialmedia.connecto.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    // Get all followings for a specific user, ordered by newest first
    Page<Follow> findAllByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    // Get all followers if a specific user, ordered by newest first
    Page<Follow> findAllByFollowedIdOrderByCreatedAtDesc(Long followedId, Pageable pageable);

    @Query(
            value = """
            SELECT u.* 
            FROM users u
            WHERE EXISTS (
                SELECT 1
                FROM follows f1
                WHERE f1.follower_id IN (
                    SELECT f2.followed_id
                    FROM follows f2
                    WHERE f2.follower_id = :currentUserId
                )
                AND f1.followed_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM follows f
                WHERE f.follower_id = :currentUserId
                AND f.followed_id = u.id
            )
            AND u.id <> :currentUserId
            AND NOT EXISTS (
                SELECT 1
                FROM follow_requests fr
                WHERE fr.follower_id = :currentUserId
                AND fr.followed_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blocks b
                WHERE b.blocker_id = :currentUserId
                AND b.blocked_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blocks b
                WHERE b.blocked_id = :currentUserId
                AND b.blocker_id = u.id
            )
            ORDER BY u.id
            """,
            countQuery = """
            SELECT COUNT(*) 
            FROM users u
            WHERE EXISTS (
                SELECT 1
                FROM follows f1
                WHERE f1.follower_id IN (
                    SELECT f2.followed_id
                    FROM follows f2
                    WHERE f2.follower_id = :currentUserId
                )
                AND f1.followed_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM follows f
                WHERE f.follower_id = :currentUserId
                AND f.followed_id = u.id
            )
            AND u.id <> :currentUserId
            AND NOT EXISTS (
                SELECT 1
                FROM follow_requests fr
                WHERE fr.follower_id = :currentUserId
                AND fr.followed_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blocks b
                WHERE b.blocker_id = :currentUserId
                AND b.blocked_id = u.id
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blocks b
                WHERE b.blocked_id = :currentUserId
                AND b.blocker_id = u.id
            )
            """,
            nativeQuery = true
    )
    Page<User> findSuggestedUsersToFollow(@Param("currentUserId") Long currentUserId, Pageable pageable);

}
