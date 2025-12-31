package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.auth.JwtUtil;
import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest // loads the full application context
@AutoConfigureMockMvc // enables MockMvc auto-configuration
@ActiveProfiles("test") // use application-test.properties
public class PostControllerTests {

    @Autowired
    private MockMvc mockMvc; // allows us to send fake HTTP requests

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RepostRepository repostRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private SavedPostRepository savedPostRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void cleanDB() {
        // clean DB before each test
        savedPostRepository.deleteAll();
        blockRepository.deleteAll();
        followRepository.deleteAll();
        repostRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

//    Follow the AAA pattern:
//
//    Arrange → Set up data and mocks.
//
//    Act → Call the method you want to test.
//
//    Assert → Verify the result.
//
//    Naming convention:
//
//    methodName_ShouldDoSomething_WhenCondition()
//
//    test: HTTP status - response JSON - DB changes

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void createPost_ShouldReturn201_WhenValidInput() throws Exception {
        User savedUser = createAndSaveTheUser();

        String requestBody = """
            {
                "content": "This is my first post!"
            }
        """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.content").value("This is my first post!"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value("Test User"))
                .andExpect(jsonPath("$.createdAt").exists());

        List<Post> posts = postRepository.findAll();

        assertEquals(1, posts.size());
        assertEquals("This is my first post!", posts.getFirst().getContent());
        assertEquals(savedUser.getId(), posts.getFirst().getUser().getId());
        assertNotNull(posts.getFirst().getCreatedAt());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void createPost_ShouldReturn400BadRequest_WhenContentIsBlank() throws Exception {
        User savedUser = createAndSaveTheUser();

        String requestBody = """
            {
                "content": " "
            }
        """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Content cannot be empty"));

        assertEquals(0, postRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updatePost_ShouldReturn200OkAndUpdatedPost_WhenValidInput() throws Exception {
        User savedUser = createAndSaveTheUser();

        Post savedPost = createAndSavePost(savedUser);

        String requestBody = """
            {
                "content": "updated"
            }
        """;

        mockMvc.perform(put("/api/posts/" +  savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.content").value("updated"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value(savedUser.getName()))
                .andExpect(jsonPath("$.createdAt").exists());

        Optional<Post> post = postRepository.findById(savedPost.getId());

        assertTrue(post.isPresent());
        assertEquals("updated", post.get().getContent());
        assertEquals(savedUser.getId(), post.get().getUser().getId());
        assertNotNull(post.get().getCreatedAt());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updatePost_ShouldReturn404NotFound_WhenPostNotFound() throws Exception {
        User savedUser = createAndSaveTheUser();

        String requestBody = """
            {
                "content": "updated"
            }
        """;

        mockMvc.perform(put("/api/posts/" +  100)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));

        assertEquals(0, postRepository.count());
    }

    @Test
    @WithMockUser(username = "test1@example.com", roles = {"USER"})
    void updatePost_ShouldReturn403Forbidden_WhenPostNotOwnedByUser() throws Exception {
        User savedUser = createAndSaveTheUser();

        Post savedPost = createAndSavePost(savedUser);

        User user = new User();
        user.setEmail("test1@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test1 User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setLocation("Saudi Arabia");
        user.setBio("my bio");

        userRepository.save(user);

        String requestBody = """
            {
                "content": "updated"
            }
        """;

        mockMvc.perform(put("/api/posts/" +  savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You can only edit your posts"));

        Post post = postRepository.findById(savedPost.getId()).get();
        assertEquals(savedPost.getContent(), post.getContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updatePost_ShouldReturn400BadRequest_WhenContentIsBlank() throws Exception {
        User savedUser = createAndSaveTheUser();

        Post savedPost = createAndSavePost(savedUser);

        String requestBody = """
            {
                "content": " "
            }
        """;

        mockMvc.perform(put("/api/posts/" +  savedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Content cannot be empty"));

        Post post = postRepository.findById(savedPost.getId()).get();
        assertEquals(savedPost.getContent(), post.getContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn404NotFound_WhenPostDoesNotExist() throws Exception {
        User savedUser = createAndSaveTheUser();

        mockMvc.perform(get("/api/posts/1/reposts"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn403Forbidden_WhenPosterIsPrivateAndUserNotFollowing() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        poster.setPrivate(true);
        poster = userRepository.save(poster);

        Post post = createAndSavePost(poster);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a private user you are not following"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn403Forbidden_WhenPosterIsBlockedByUser() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        Post post = createAndSavePost(poster);

        createAndSaveBlock(savedUser, poster);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a user you blocked"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn403Forbidden_WhenPosterBlocksUser() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        Post post = createAndSavePost(poster);

        createAndSaveBlock(poster, savedUser);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a user that blocked you"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn200OkAndEmptyList_WhenNoReposts() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        Post post = createAndSavePost(poster);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn200OkAndPagedList_WhenRepostsExist() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        Post post = createAndSavePost(poster);

        User reposter1 = createAndSaveUser();
        Repost repost1 = createAndSaveRepost(reposter1, post);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        User reposter2 = createAndSaveUser();
        Repost repost2 = createAndSaveRepost(reposter2, post);

        User reposter3 = createAndSaveUser();
        Repost repost3 = createAndSaveRepost(reposter3, post);
        createAndSaveBlock(savedUser, reposter3);

        User reposter4 = createAndSaveUser();
        Repost repost4 = createAndSaveRepost(reposter4, post);
        createAndSaveBlock(reposter4, savedUser);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(reposter2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(reposter2.getName()))
                .andExpect(jsonPath("$.list[0].repostedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(reposter1.getId()))
                .andExpect(jsonPath("$.list[1].name").value(reposter1.getName()))
                .andExpect(jsonPath("$.list[1].repostedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getReposters_ShouldReturn200OkAndPagedList_WhenRepostsExistAndPosterPrivateAndUserFollowsPoster() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        poster.setPrivate(true);
        poster = userRepository.save(poster);
        createAndSaveFollow(savedUser, poster);
        Post post = createAndSavePost(poster);

        User reposter1 = createAndSaveUser();
        createAndSaveFollow(reposter1, poster);
        Repost repost1 = createAndSaveRepost(reposter1, post);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        User reposter2 = createAndSaveUser();
        createAndSaveFollow(reposter2, poster);
        Repost repost2 = createAndSaveRepost(reposter2, post);

        User reposter3 = createAndSaveUser();
        createAndSaveFollow(reposter3, poster);
        Repost repost3 = createAndSaveRepost(reposter3, post);
        createAndSaveBlock(savedUser, reposter3);

        User reposter4 = createAndSaveUser();
        createAndSaveFollow(reposter4, poster);
        Repost repost4 = createAndSaveRepost(reposter4, post);
        createAndSaveBlock(reposter4, savedUser);

        mockMvc.perform(get("/api/posts/" + post.getId() + "/reposts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(reposter2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(reposter2.getName()))
                .andExpect(jsonPath("$.list[0].repostedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(reposter1.getId()))
                .andExpect(jsonPath("$.list[1].name").value(reposter1.getName()))
                .andExpect(jsonPath("$.list[1].repostedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn404NotFound_WhenPostDoesNotExist() throws Exception {
        User savedUser = createAndSaveTheUser();

        mockMvc.perform(post("/api/posts/1/save"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));

        assertEquals(0, savedPostRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn403Forbidden_WhenPosterPrivateAndUserNotFollowing() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        poster.setPrivate(true);
        poster = userRepository.save(poster);

        Post post = createAndSavePost(poster);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a private user you are not following"));

        assertEquals(0, savedPostRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn403Forbidden_WhenPosterBlocksUser() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();

        createAndSaveBlock(poster, savedUser);

        Post post = createAndSavePost(poster);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a user that blocked you"));

        assertEquals(0, savedPostRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn403Forbidden_WhenUserBlocksPoster() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();

        createAndSaveBlock(savedUser, poster);

        Post post = createAndSavePost(poster);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You cannot access a post of a user you blocked"));

        assertEquals(0, savedPostRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn200Ok_WhenPostAlreadySaved() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();

        Post post = createAndSavePost(poster);

        createAndSaveSavedPost(savedUser, post);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post saved successfully"));

        assertEquals(1, savedPostRepository.count());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn200OkAndSavePost_WhenPosterIsCurrentUser() throws Exception {
        User savedUser = createAndSaveTheUser();

        Post post = createAndSavePost(savedUser);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post saved successfully"));

        assertTrue(savedPostRepository.existsByUserIdAndPostId(savedUser.getId(), post.getId()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn200OkAndSavePost_WhenPosterIsPublic() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();

        Post post = createAndSavePost(poster);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post saved successfully"));

        assertTrue(savedPostRepository.existsByUserIdAndPostId(savedUser.getId(), post.getId()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void savePost_ShouldReturn200OkAndSavePost_WhenPosterPrivateAndUserFollowing() throws Exception {
        User savedUser = createAndSaveTheUser();

        User poster = createAndSaveUser();
        poster.setPrivate(true);
        poster = userRepository.save(poster);

        createAndSaveFollow(savedUser, poster);

        Post post = createAndSavePost(poster);

        mockMvc.perform(post("/api/posts/" + post.getId() + "/save"))
                .andExpect(status().isOk())
                .andExpect(content().string("Post saved successfully"));

        assertTrue(savedPostRepository.existsByUserIdAndPostId(savedUser.getId(), post.getId()));
    }

    private SavedPost createAndSaveSavedPost(User user, Post post) {
        SavedPost savedPost = new SavedPost();
        savedPost.setUser(user);
        savedPost.setPost(post);
        return savedPostRepository.save(savedPost);
    }

    private Post createAndSavePost(User savedUser) {
        Post post = new Post();
        post.setContent("This is my first post!");
        post.setUser(savedUser);
        return postRepository.save(post);
    }

    private User createAndSaveTheUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setLocation("Saudi Arabia");
        user.setBio("my bio");

        return userRepository.save(user);
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

    private Repost createAndSaveRepost(User user, Post post) {
        Repost repost = new Repost();
        repost.setReposter(user);
        repost.setPost(post);
        return repostRepository.save(repost);
    }

    private Block createAndSaveBlock(User blocker, User blocked) {
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        block = blockRepository.save(block);
        return block;
    }

    private Follow createAndSaveFollow(User follower, User followed) {
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        return followRepository.save(follow);
    }

}
