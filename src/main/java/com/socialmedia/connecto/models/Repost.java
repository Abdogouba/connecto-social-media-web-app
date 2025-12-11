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
        name = "reposts",
        indexes = {
                @Index(name = "idx_repost_reposter", columnList = "reposter_id"),
                @Index(name = "idx_repost_post", columnList = "post_id")
        }
)
public class Repost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reposter_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_repost_reposter"),
            referencedColumnName = "id")
    private User reposter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_repost_post"),
            referencedColumnName = "id")
    private Post post;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

