package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Block;
import com.socialmedia.connecto.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);


}
