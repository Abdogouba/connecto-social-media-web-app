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

        User savedUser = userRepository.save(user);

        String requestBody = """
            {
                "content": "This is my first post!"
            }
        """;

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("This is my first post!"))
                .andExpect(jsonPath("$.userId").value(savedUser.getId()))
                .andExpect(jsonPath("$.userName").value("Test User"))
                .andExpect(jsonPath("$.createdAt").exists());

        Optional<Post> post = postRepository.findById(1L);

        assertTrue(post.isPresent());
        assertEquals("This is my first post!", post.get().getContent());
        assertEquals(savedUser.getId(), post.get().getUser().getId());
        assertNotNull(post.get().getCreatedAt());
    }

}
