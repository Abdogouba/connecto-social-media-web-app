package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Post;
import com.socialmedia.connecto.models.SavedPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    void deleteByUserIdAndPostId(Long userId, Long postId);
}
