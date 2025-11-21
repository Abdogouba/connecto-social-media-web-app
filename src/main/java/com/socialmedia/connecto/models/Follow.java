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
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follower_followed",
                        columnNames = {"follower_id", "followed_id"}
                )
        },
        indexes = {
                @Index(name = "idx_follower", columnList = "follower_id"),
                @Index(name = "idx_followed", columnList = "followed_id")
        }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_follower"),
            referencedColumnName = "id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_followed"),
            referencedColumnName = "id")
    private User followed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
