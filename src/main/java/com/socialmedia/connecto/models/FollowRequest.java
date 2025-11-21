package com.socialmedia.connecto.models;

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
        name = "follow_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follower_followed_2",
                        columnNames = {"follower_id", "followed_id"}
                )
        },
        indexes = {
                @Index(name = "idx_follower_2", columnList = "follower_id"),
                @Index(name = "idx_followed_2", columnList = "followed_id")
        }
)
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_follower_2"),
            referencedColumnName = "id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_followed_2"),
            referencedColumnName = "id")
    private User followed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

