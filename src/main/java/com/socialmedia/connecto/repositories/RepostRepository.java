package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.models.Repost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {
}
