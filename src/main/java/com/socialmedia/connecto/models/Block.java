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
        name = "blocks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blocker_blocked",
                        columnNames = {"blocker_id", "blocked_id"}
                )
        },
        indexes = {
                @Index(name = "idx_blocker", columnList = "blocker_id"),
                @Index(name = "idx_blocked", columnList = "blocked_id")
        }
)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_blocker"),
            referencedColumnName = "id")
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_blocked"),
            referencedColumnName = "id")
    private User blocked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

