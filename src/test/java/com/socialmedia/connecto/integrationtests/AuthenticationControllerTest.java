package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.models.Gender;
import com.socialmedia.connecto.models.Role;
import com.socialmedia.connecto.models.User;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // loads the full application context
@AutoConfigureMockMvc // enables MockMvc auto-configuration
@ActiveProfiles("test") // use application-test.properties
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc; // allows us to send fake HTTP requests

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDB() {
        this.userRepository.deleteAll(); // clean DB before each test
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
    void register_shouldCreateUser_WhenValidInput() throws Exception {

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "abdullah@example.com",
                "password": "mypassword123",
                "gender": "MALE",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding",
                "pictureURL": "url"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        Optional<User> user = userRepository.findByEmail("abdullah@example.com");

        assertTrue(user.isPresent());

        assertEquals("Abdullah", user.get().getName());
        assertEquals("MALE", user.get().getGender().name());
        assertEquals("New Cairo, Cairo, Egypt", user.get().getLocation());
        assertEquals("just coding", user.get().getBio());
        assertEquals("url", user.get().getPictureURL());
        assertEquals(LocalDate.parse("2002-04-11"), user.get().getBirthDate());
        assertEquals(Role.USER, user.get().getRole());
        assertTrue(passwordEncoder.matches("mypassword123", user.get().getPassword()));
        assertFalse(user.get().isPrivate());
        assertFalse(user.get().isBanned());

    }

    @Test
    void register_shouldCreateUser_WhenValidInputWithSomeNulls() throws Exception {

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "abdullah@example.com",
                "password": "mypassword123",
                "gender": "MALE"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        Optional<User> user = userRepository.findByEmail("abdullah@example.com");

        assertTrue(user.isPresent());

        assertEquals("Abdullah", user.get().getName());
        assertEquals("MALE", user.get().getGender().name());
        assertNull(user.get().getLocation());
        assertNull(user.get().getBio());
        assertNull(user.get().getPictureURL());
        assertNull(user.get().getBirthDate());
        assertEquals(Role.USER, user.get().getRole());
        assertTrue(passwordEncoder.matches("mypassword123", user.get().getPassword()));
        assertFalse(user.get().isPrivate());
        assertFalse(user.get().isBanned());

    }

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() throws Exception {

        this.userRepository.save(new User(null, "Abdullah",
                "duplicate@example.com", "password",
                Gender.MALE, null, null, null,
                false, false, Role.USER, null, null));

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "duplicate@example.com",
                "password": "mypassword123",
                "gender": "MALE",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding",
                "pictureURL": "url"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already registered"));

        assertEquals(1, userRepository.count());
    }

    @Test
    void register_ShouldFail_WhenInvalidGender() throws Exception {

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "duplicate@example.com",
                "password": "mypassword123",
                "gender": "M",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding",
                "pictureURL": "url"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid gender"));

        assertEquals(0, userRepository.count());
    }

    @Test
    void register_ShouldFail_WhenNameIsBlank() throws Exception {

        String requestBody = """
            {
                "name": "",
                "email": "duplicate@example.com",
                "password": "mypassword123",
                "gender": "MALE",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding",
                "pictureURL": "url"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name is required"));

        assertEquals(0, userRepository.count());
    }

}
