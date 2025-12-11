package com.socialmedia.connecto.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    private LocalDate birthDate;

    @Column(length = 50)
    private String location;

    @Column(length = 300)
    private String bio;

    @Column(nullable = false)
    private boolean isPrivate = false;

    @Column(nullable = false)
    private boolean isBanned = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> receivedNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> sentNotifications = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> following = new ArrayList<>();

    @OneToMany(mappedBy = "followed", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> followers = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FollowRequest> followRequestsSent = new ArrayList<>();

    @OneToMany(mappedBy = "followed", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FollowRequest> followRequestsReceived = new ArrayList<>();

    @OneToMany(mappedBy = "blocker", cascade = CascadeType.REMOVE)
    private List<Block> blocksMade = new ArrayList<>();

    @OneToMany(mappedBy = "blocked", cascade = CascadeType.REMOVE)
    private List<Block> blocksReceived = new ArrayList<>();

    @OneToMany(mappedBy = "reposter", cascade = CascadeType.REMOVE)
    private List<Repost> reposts = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
