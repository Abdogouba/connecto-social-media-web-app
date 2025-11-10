package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.auth.JwtUtil;
import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Post;
import com.socialmedia.connecto.models.Role;
import com.socialmedia.connecto.models.User;
import com.socialmedia.connecto.repositories.PostRepository;
import com.socialmedia.connecto.repositories.UserRepository;
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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void cleanDB() {
        // clean DB before each test
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
        User savedUser = createAndSaveUser();

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
        User savedUser = createAndSaveUser();

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
        User savedUser = createAndSaveUser();

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
        User savedUser = createAndSaveUser();

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
        User savedUser = createAndSaveUser();

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
        User savedUser = createAndSaveUser();

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

    private Post createAndSavePost(User savedUser) {
        Post post = new Post();
        post.setContent("This is my first post!");
        post.setUser(savedUser);
        return postRepository.save(post);
    }

    private User createAndSaveUser() {
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

}
