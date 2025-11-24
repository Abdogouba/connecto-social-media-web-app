package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FollowControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private FollowRequestRepository  followRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private User follower;
    private User followed;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        followRequestRepository.deleteAll();
        followRepository.deleteAll();
        blockRepository.deleteAll();
        userRepository.deleteAll();

        // Create follower
        follower = new User();
        follower.setEmail("follower@example.com");
        follower.setPassword(passwordEncoder.encode("password"));
        follower.setName("follower");
        follower.setRole(Role.USER);
        follower.setGender(Gender.MALE);
        follower.setPrivate(false);
        follower.setBanned(false);
        follower.setBirthDate(LocalDate.of(2000, 1, 1));
        follower = userRepository.save(follower);

        // Create followed
        followed = new User();
        followed.setEmail("followed@example.com");
        followed.setPassword(passwordEncoder.encode("password"));
        followed.setName("followed");
        followed.setRole(Role.USER);
        followed.setGender(Gender.MALE);
        followed.setPrivate(false);
        followed.setBanned(false);
        followed.setBirthDate(LocalDate.of(2000, 1, 1));
        followed = userRepository.save(followed);
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn400BadRequest_WhenUserFollowsHimself() throws Exception {
        mockMvc.perform(post("/api/follows/" + follower.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User cannot follow himself"));

        assertEquals(0, followRepository.count());
        assertEquals(0, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn404NotFound_WhenToBeFollowedNotFound() throws Exception {
        mockMvc.perform(post("/api/follows/" + 11111)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User to be followed not found"));

        assertEquals(0, followRepository.count());
        assertEquals(0, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn409Conflict_WhenUserBlocksToBeFollowed() throws Exception {
        createAndSaveBlock(follower, followed);

        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User cannot follow a user he blocked"));

        assertEquals(0, followRepository.count());
        assertEquals(0, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn403Forbidden_WhenUserIsBlockedByToBeFollowed() throws Exception {
        createAndSaveBlock(followed, follower);

        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot follow a user that blocked him"));

        assertEquals(0, followRepository.count());
        assertEquals(0, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn409Conflict_WhenUserAlreadyFollowsTheTarget() throws Exception {
        createAndSaveFollow();

        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already follows this user"));

        assertEquals(1, followRepository.count());
        assertEquals(0, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn409Conflict_WhenUserAlreadySentFollowRequest() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);
        createAndSaveFollowRequest();

        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already sent a follow request"));

        assertEquals(0, followRepository.count());
        assertEquals(1, followRequestRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn200Ok_WhenUserSendsAValidFollowRequest() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Follow request sent"));

        List<Notification> notifications = notificationRepository.findAll();

        assertEquals(1, notifications.size());
        assertEquals(followed.getId(), notifications.getFirst().getReceiver().getId());
        assertEquals(follower.getId(), notifications.getFirst().getSender().getId());
        assertEquals(NotificationType.FOLLOW_REQUEST, notifications.getFirst().getType());
        assertFalse(notifications.getFirst().isRead());
        assertEquals(0, followRepository.count());
        assertTrue(followRequestRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId()));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void follow_ShouldReturn200Ok_WhenUserSendsAValidFollow() throws Exception {
        mockMvc.perform(post("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Follow was successful"));

        List<Notification> notifications = notificationRepository.findAll();

        assertEquals(1, notifications.size());
        assertEquals(followed.getId(), notifications.getFirst().getReceiver().getId());
        assertEquals(follower.getId(), notifications.getFirst().getSender().getId());
        assertEquals(NotificationType.NEW_FOLLOWER, notifications.getFirst().getType());
        assertFalse(notifications.getFirst().isRead());
        assertEquals(0, followRequestRepository.count());
        assertTrue(followRepository.existsByFollowerIdAndFollowedId(follower.getId(), followed.getId()));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void unfollow_ShouldReturn400BadRequest_WhenUserUnfollowsHimself() throws Exception {
        mockMvc.perform(delete("/api/follows/" + follower.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User cannot unfollow himself"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void unfollow_ShouldReturn404NotFound_WhenTargetNotFound() throws Exception {
        mockMvc.perform(delete("/api/follows/" + 11111)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User to be unfollowed not found"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void unfollow_ShouldReturn204NoContent_WhenTargetValid() throws Exception {
        createAndSaveFollow();

        mockMvc.perform(delete("/api/follows/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(0, followRepository.count());
    }

    private FollowRequest createAndSaveFollowRequest() {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setFollower(follower);
        followRequest.setFollowed(followed);
        return followRequestRepository.save(followRequest);
    }

    private Follow createAndSaveFollow() {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        return followRepository.save(follow);
    }

    private Block createAndSaveBlock(User blocker, User blocked) {
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        block = blockRepository.save(block);
        return block;
    }

}

