package com.socialmedia.connecto.models;

import com.socialmedia.connecto.enums.ReactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "post_reactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_reaction",
                        columnNames = {"user_id", "post_id"}
                )
        },
        indexes = {
                @Index(name = "idx_post_reaction_post", columnList = "post_id"),
                @Index(name = "idx_post_reaction_post_type", columnList = "post_id, type"),
                @Index(name = "idx_post_reaction_user", columnList = "user_id"),
                @Index(name = "idx_post_reaction_post_created", columnList = "post_id, created_at")
        }
)
public class PostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_reaction_user"),
            referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_post_reaction_post"),
            referencedColumnName = "id")
    private Post post;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}



