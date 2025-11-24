package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    // Get all blocks for a specific blocker, ordered by newest first
    Page<Block> findAllByBlockerIdOrderByCreatedAtDesc(Long blockerId, Pageable pageable);

}
