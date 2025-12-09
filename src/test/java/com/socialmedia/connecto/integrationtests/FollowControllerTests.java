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

    @Test
    @WithMockUser(username = "followed@example.com")
    public void removeFollower_ShouldReturn403Forbidden_WhenUserNotPrivate() throws Exception {
        createAndSaveFollow();

        mockMvc.perform(delete("/api/follows/followers/" + follower.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Public users cannot remove a follower"));

        assertEquals(1, followRepository.count());
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    public void removeFollower_ShouldReturn400BadRequest_WhenUserRemovesHimself() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(delete("/api/follows/followers/" + followed.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User cannot remove himself from followers"));
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    public void removeFollower_ShouldReturn404NotFound_WhenTargetNotFound() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(delete("/api/follows/followers/" + 11111)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User to be removed from followers not found"));
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    public void removeFollower_ShouldReturn204NoContent_WhenTargetValid() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);
        createAndSaveFollow();

        mockMvc.perform(delete("/api/follows/followers/" + follower.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(0, followRepository.count());
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    public void removeFollower_ShouldReturn204NoContent_WhenTargetValidButNotFollower() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(delete("/api/follows/followers/" + follower.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturn404NotFound_WhenTargetDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/follows/" + 11111 + "/following")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowing_ShouldReturn403Forbidden_WhenUserIsBlockedByTarget() throws Exception {
        createAndSaveBlock(followed, follower);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/following")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot view following list of a user that blocked him"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowing_ShouldReturn409Conflict_WhenUserBlockedTarget() throws Exception {
        createAndSaveBlock(follower, followed);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/following")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User cannot view following list of a user he blocked"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowing_ShouldReturn403Forbidden_WhenTargetPrivateAndUserNotFollower() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/following")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot view following list of a private user he is not following"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturnFollowingUsersPaginated_WhenTargetIsCurrentUser() throws Exception {
        follower.setPrivate(true);
        follower = userRepository.save(follower);

        Follow follow1 = createAndSaveFollow();

        User followed2 = new User();
        followed2.setEmail("followed2@example.com");
        followed2.setPassword(passwordEncoder.encode("password"));
        followed2.setName("followed2");
        followed2.setRole(Role.USER);
        followed2.setGender(Gender.MALE);
        followed2.setPrivate(false);
        followed2.setBanned(false);
        followed2.setBirthDate(LocalDate.of(2000, 1, 1));
        followed2 = userRepository.save(followed2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Follow follow2 = new Follow();
        follow2.setFollower(follower);
        follow2.setFollowed(followed2);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + follower.getId() + "/following")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(followed2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(followed2.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(followed.getId()))
                .andExpect(jsonPath("$.list[1].name").value(followed.getName()))
                .andExpect(jsonPath("$.list[1].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturnFollowingUsersPaginated_WhenTargetPrivateAndUserFollowsTarget() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        createAndSaveFollow();

        User followed2 = new User();
        followed2.setEmail("followed2@example.com");
        followed2.setPassword(passwordEncoder.encode("password"));
        followed2.setName("followed2");
        followed2.setRole(Role.USER);
        followed2.setGender(Gender.MALE);
        followed2.setPrivate(false);
        followed2.setBanned(false);
        followed2.setBirthDate(LocalDate.of(2000, 1, 1));
        followed2 = userRepository.save(followed2);

        Follow follow2 = new Follow();
        follow2.setFollower(followed);
        follow2.setFollowed(followed2);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + followed.getId() + "/following")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.list[0].id").value(followed2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(followed2.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(1));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturnEmpty_WhenNoFollowingUsers() throws Exception {
        mockMvc.perform(get("/api/follows/" + follower.getId() + "/following")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturnEmpty_WhenPageNumberVeyLarge() throws Exception {
        Follow follow1 = createAndSaveFollow();

        User followed2 = new User();
        followed2.setEmail("followed2@example.com");
        followed2.setPassword(passwordEncoder.encode("password"));
        followed2.setName("followed2");
        followed2.setRole(Role.USER);
        followed2.setGender(Gender.MALE);
        followed2.setPrivate(false);
        followed2.setBanned(false);
        followed2.setBirthDate(LocalDate.of(2000, 1, 1));
        followed2 = userRepository.save(followed2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Follow follow2 = new Follow();
        follow2.setFollower(follower);
        follow2.setFollowed(followed2);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + follower.getId() + "/following")
                        .param("page", "3")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.currentPage").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowing_ShouldReturnFollowingUsersPaginated_WhenPageSizeVeryLarge() throws Exception {
        Follow follow1 = createAndSaveFollow();

        User followed2 = new User();
        followed2.setEmail("followed2@example.com");
        followed2.setPassword(passwordEncoder.encode("password"));
        followed2.setName("followed2");
        followed2.setRole(Role.USER);
        followed2.setGender(Gender.MALE);
        followed2.setPrivate(false);
        followed2.setBanned(false);
        followed2.setBirthDate(LocalDate.of(2000, 1, 1));
        followed2 = userRepository.save(followed2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Follow follow2 = new Follow();
        follow2.setFollower(follower);
        follow2.setFollowed(followed2);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + follower.getId() + "/following")
                        .param("page", "0")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(followed2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(followed2.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(followed.getId()))
                .andExpect(jsonPath("$.list[1].name").value(followed.getName()))
                .andExpect(jsonPath("$.list[1].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowers_ShouldReturn404NotFound_WhenTargetDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/follows/" + 11111 + "/followers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowers_ShouldReturn403Forbidden_WhenUserIsBlockedByTarget() throws Exception {
        createAndSaveBlock(followed, follower);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/followers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot view followers list of a user that blocked him"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowers_ShouldReturn409Conflict_WhenUserBlockedTarget() throws Exception {
        createAndSaveBlock(follower, followed);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/followers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("User cannot view followers list of a user he blocked"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    public void getFollowers_ShouldReturn403Forbidden_WhenTargetPrivateAndUserNotFollower() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(get("/api/follows/" + followed.getId() +  "/followers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot view followers list of a private user he is not following"));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowers_ShouldReturnFollowersPaginated_WhenTargetIsCurrentUser() throws Exception {
        follower.setPrivate(true);
        follower = userRepository.save(follower);

        Follow follow1 = new Follow();
        follow1.setFollower(followed);
        follow1.setFollowed(follower);
        follow1 = followRepository.save(follow1);

        User follower2 = new User();
        follower2.setEmail("followed2@example.com");
        follower2.setPassword(passwordEncoder.encode("password"));
        follower2.setName("followed2");
        follower2.setRole(Role.USER);
        follower2.setGender(Gender.MALE);
        follower2.setPrivate(false);
        follower2.setBanned(false);
        follower2.setBirthDate(LocalDate.of(2000, 1, 1));
        follower2 = userRepository.save(follower2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Follow follow2 = new Follow();
        follow2.setFollower(follower2);
        follow2.setFollowed(follower);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + follower.getId() + "/followers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(follower2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(follower2.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(followed.getId()))
                .andExpect(jsonPath("$.list[1].name").value(followed.getName()))
                .andExpect(jsonPath("$.list[1].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowers_ShouldReturnFollowersPaginated_WhenTargetPrivateAndUserFollowsTarget() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        createAndSaveFollow();

        User follower2 = new User();
        follower2.setEmail("followed2@example.com");
        follower2.setPassword(passwordEncoder.encode("password"));
        follower2.setName("followed2");
        follower2.setRole(Role.USER);
        follower2.setGender(Gender.MALE);
        follower2.setPrivate(false);
        follower2.setBanned(false);
        follower2.setBirthDate(LocalDate.of(2000, 1, 1));
        follower2 = userRepository.save(follower2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Follow follow2 = new Follow();
        follow2.setFollower(follower2);
        follow2.setFollowed(followed);
        follow2 = followRepository.save(follow2);

        mockMvc.perform(get("/api/follows/" + followed.getId() + "/followers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(follower2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(follower2.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(follower.getId()))
                .andExpect(jsonPath("$.list[1].name").value(follower.getName()))
                .andExpect(jsonPath("$.list[1].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowers_ShouldReturnEmptyList_WhenNoFollowers() throws Exception {
        mockMvc.perform(get("/api/follows/" + followed.getId() + "/followers")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowSuggestions_ShouldReturnEmptyList_WhenNotFollowingAnyone() throws Exception {
        mockMvc.perform(get("/api/follows/suggestions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "follower@example.com")
    void getFollowSuggestions_ShouldReturnPaginatedList_WhenSuggestionsExist() throws Exception {
        // current user follows 2 users
        createAndSaveFollow();
        User user1 = createAndSaveUser();
        createAndSaveFollow(follower, user1);

        // users who are followed by current user follow 2 users that will be in result list
        User user2 = createAndSaveUser();
        createAndSaveFollow(followed, user2);
        User user3 = createAndSaveUser();
        createAndSaveFollow(user1, user3);

        // duplicate suggestion
        createAndSaveFollow(user1, user2);

        // suggestion that current user follows
        createAndSaveFollow(user1, followed);

        // current user suggestion
        createAndSaveFollow(user1, follower);

        // suggestion that current user sent a follow request
        User user4 = createAndSaveUser();
        user4.setPrivate(true);
        user4 = userRepository.save(user4);
        createAndSaveFollowRequest(follower, user4);
        createAndSaveFollow(followed, user4);

        // suggestion that the current user blocked
        User user5 = createAndSaveUser();
        createAndSaveBlock(follower, user5);
        createAndSaveFollow(followed, user5);

        // suggestion that blocked the current user
        User user6 = createAndSaveUser();
        createAndSaveBlock(user6, follower);
        createAndSaveFollow(followed, user6);

        mockMvc.perform(get("/api/follows/suggestions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(user2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(user2.getName()))
                .andExpect(jsonPath("$.list[0].private").value(false))
                .andExpect(jsonPath("$.list[1].id").value(user3.getId()))
                .andExpect(jsonPath("$.list[1].name").value(user3.getName()))
                .andExpect(jsonPath("$.list[1].private").value(false))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    private FollowRequest createAndSaveFollowRequest(User user1, User user2) {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setFollower(user1);
        followRequest.setFollowed(user2);
        return followRequestRepository.save(followRequest);
    }

    private Follow createAndSaveFollow(User user1, User user2) {
        Follow follow = new Follow();
        follow.setFollower(user1);
        follow.setFollowed(user2);
        return followRepository.save(follow);
    }

    private User createAndSaveUser() {
        long count = userRepository.count();
        User user = new User();
        user.setEmail("user" + count + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("user" + count);
        user.setRole(Role.USER);
        user.setGender(Gender.MALE);
        user.setPrivate(false);
        user.setBanned(false);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        return userRepository.save(user);
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

