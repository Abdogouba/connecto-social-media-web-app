package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    Optional<PostReaction> findByUserIdAndPostId(Long userId, Long postId);
}
