package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.enums.Gender;
import com.socialmedia.connecto.enums.NotificationType;
import com.socialmedia.connecto.enums.Role;
import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
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
public class RepostControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RepostRepository repostRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    private User poster;
    private User reposter;
    private Post post;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        repostRepository.deleteAll();
        postRepository.deleteAll();
        blockRepository.deleteAll();
        userRepository.deleteAll();

        // Create poster
        poster = new User();
        poster.setEmail("poster@example.com");
        poster.setPassword(passwordEncoder.encode("password"));
        poster.setName("poster");
        poster.setRole(Role.USER);
        poster.setGender(Gender.MALE);
        poster.setPrivate(false);
        poster.setBanned(false);
        poster.setBirthDate(LocalDate.of(2000, 1, 1));
        poster = userRepository.save(poster);

        // Create reposter
        reposter = new User();
        reposter.setEmail("reposter@example.com");
        reposter.setPassword(passwordEncoder.encode("password"));
        reposter.setName("reposter");
        reposter.setRole(Role.USER);
        reposter.setGender(Gender.MALE);
        reposter.setPrivate(false);
        reposter.setBanned(false);
        reposter.setBirthDate(LocalDate.of(2000, 1, 1));
        reposter = userRepository.save(reposter);

        // Create post
        post = new Post();
        post.setUser(poster);
        post.setContent("content");
        post = postRepository.save(post);
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn404NotFound_WhenPostIsNotFound() throws Exception {
        postRepository.deleteAll();

        mockMvc.perform(post("/api/reposts/" + 1))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));

        assertEquals(0, repostRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn403Forbidden_WhenPosterIsPrivate() throws Exception {
        poster.setPrivate(true);
        poster = userRepository.save(poster);

        mockMvc.perform(post("/api/reposts/" + post.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot repost a post that belongs to a private user"));

        assertEquals(0, repostRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn403Forbidden_WhenReposterBlockedPoster() throws Exception {
        createAndSaveBlock(reposter, poster);

        mockMvc.perform(post("/api/reposts/" + post.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot repost this post, you blocked post owner"));

        assertEquals(0, repostRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn403Forbidden_WhenReposterIsBlockedByPoster() throws Exception {
        createAndSaveBlock(poster, reposter);

        mockMvc.perform(post("/api/reposts/" + post.getId()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot repost this post, post owner blocked you"));

        assertEquals(0, repostRepository.count());
        assertEquals(0, notificationRepository.count());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn201Created_WhenReposterIsPoster() throws Exception {
        Post post1 = createAndSavePost(reposter);

        mockMvc.perform(post("/api/reposts/" + post1.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reposterId").value(reposter.getId()))
                .andExpect(jsonPath("$.reposterName").value(reposter.getName()))
                .andExpect(jsonPath("$.postId").value(post1.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        assertEquals(1, repostRepository.count());
        assertEquals(0, notificationRepository.count());
        List<Repost> reposts = repostRepository.findAll();
        Repost repost = reposts.getFirst();
        assertEquals(reposter.getId(), repost.getReposter().getId());
        assertEquals(post1.getId(), repost.getPost().getId());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void repost_ShouldReturn201Created_WhenRepostsSamePostMoreThanOnce() throws Exception {
        mockMvc.perform(post("/api/reposts/" + post.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reposterId").value(reposter.getId()))
                .andExpect(jsonPath("$.reposterName").value(reposter.getName()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        mockMvc.perform(post("/api/reposts/" + post.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.reposterId").value(reposter.getId()))
                .andExpect(jsonPath("$.reposterName").value(reposter.getName()))
                .andExpect(jsonPath("$.postId").value(post.getId()))
                .andExpect(jsonPath("$.createdAt").exists());

        assertEquals(2, repostRepository.count());
        assertEquals(2, notificationRepository.count());
        List<Repost> reposts = repostRepository.findAll(Sort.by("createdAt"));
        List<Notification> notifications = notificationRepository.findAll(Sort.by("createdAt"));
        Repost repost1 = reposts.get(0);
        Repost repost2 = reposts.get(1);
        Notification notification1 = notifications.get(0);
        Notification notification2 = notifications.get(1);
        assertEquals(reposter.getId(), repost1.getReposter().getId());
        assertEquals(reposter.getId(), repost2.getReposter().getId());
        assertEquals(post.getId(), repost1.getPost().getId());
        assertEquals(post.getId(), repost2.getPost().getId());
        assertEquals(poster.getId(), notification1.getReceiver().getId());
        assertEquals(poster.getId(), notification2.getReceiver().getId());
        assertEquals(reposter.getId(), notification1.getSender().getId());
        assertEquals(reposter.getId(), notification2.getSender().getId());
        assertEquals(NotificationType.SHARED_POST, notification1.getType());
        assertEquals(NotificationType.SHARED_POST, notification2.getType());
        assertEquals(post.getId(), notification1.getReferenceId());
        assertEquals(post.getId(), notification2.getReferenceId());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void deleteRepost_ShouldReturn204NoContent_WhenRepostExistsAndBelongsToUser() throws Exception {
        Repost repost = createAndSaveRepost(reposter, post);

        mockMvc.perform(delete("/api/reposts/" + repost.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, repostRepository.count());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void deleteRepost_ShouldReturn204NoContent_WhenRepostDoesNotExists() throws Exception {
        mockMvc.perform(delete("/api/reposts/" + 5))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "reposter@example.com")
    void deleteRepost_ShouldReturn403Forbidden_WhenRepostDoesNotBelongToUser() throws Exception {
        Repost repost = createAndSaveRepost(poster, post);

        mockMvc.perform(delete("/api/reposts/" + repost.getId()))
                .andExpect(status().isForbidden());

        assertEquals(1, repostRepository.count());
    }

    private int getRandomPositiveInt() {
        return (int) (Math.random() * Integer.MAX_VALUE) + 1;
    }

    private User createAndSaveUser() {
        long random = getRandomPositiveInt();
        User user = new User();
        user.setEmail("user" + random + "@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setName("user" + random);
        user.setRole(Role.USER);
        user.setGender(Gender.MALE);
        user.setPrivate(false);
        user.setBanned(false);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        return userRepository.save(user);
    }

    private Block createAndSaveBlock(User blocker, User blocked) {
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        block = blockRepository.save(block);
        return block;
    }

    private Post createAndSavePost(User user) {
        Post post = new Post();
        post.setUser(user);
        post.setContent("content");
        return postRepository.save(post);
    }

    private Repost createAndSaveRepost(User user, Post post) {
        Repost repost = new Repost();
        repost.setReposter(user);
        repost.setPost(post);
        return repostRepository.save(repost);
    }

}


