package com.socialmedia.connecto.repositories;

import com.socialmedia.connecto.dtos.ReposterDTO;
import com.socialmedia.connecto.models.Repost;
import com.socialmedia.connecto.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepostRepository extends JpaRepository<Repost, Long> {

    @Query(
            value = """
    SELECT new com.socialmedia.connecto.dtos.ReposterDTO(
        u.id,
        u.name,
        r.createdAt
    )
    FROM Repost r
    JOIN r.reposter u
    WHERE r.post.id = :postId
      AND NOT EXISTS (
          SELECT 1
          FROM Block b
          WHERE (b.blocker.id = :currentUserId AND b.blocked.id = u.id)
             OR (b.blocker.id = u.id AND b.blocked.id = :currentUserId)
      )
    ORDER BY r.createdAt DESC
    """,
            countQuery = """
    SELECT COUNT(r)
    FROM Repost r
    JOIN r.reposter u
    WHERE r.post.id = :postId
      AND NOT EXISTS (
          SELECT 1
          FROM Block b
          WHERE (b.blocker.id = :currentUserId AND b.blocked.id = u.id)
             OR (b.blocker.id = u.id AND b.blocked.id = :currentUserId)
      )
    """
    )
    Page<ReposterDTO> findRepostersExcludingBlocked(
            @Param("currentUserId") Long currentUserId,
            @Param("postId") Long postId,
            Pageable pageable
    );

}
