package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.auth.JwtUtil;
import com.socialmedia.connecto.enums.Gender;
import com.socialmedia.connecto.enums.Role;
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


import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest // loads the full application context
@AutoConfigureMockMvc // enables MockMvc auto-configuration
@ActiveProfiles("test") // use application-test.properties
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc; // allows us to send fake HTTP requests

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void editProfile_ShouldUpdateSuccessfully_WhenInputValid() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "name": "Abdullah",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding",
                "isPrivate": true
            }
        """;

        mockMvc.perform(put("/api/users/edit-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));

        User updatedUser = userRepository.findByEmail(user.getEmail()).get();

        assertEquals("Abdullah", updatedUser.getName());
        assertEquals("New Cairo, Cairo, Egypt", updatedUser.getLocation());
        assertEquals("just coding", updatedUser.getBio());
        assertTrue(updatedUser.isPrivate());

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void editProfile_ShouldFail_WhenNameIsBlank() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "name": "",
                "location": "location",
                "bio": "bio",
                "isPrivate": false
            }
        """;

        mockMvc.perform(put("/api/users/edit-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name is required"));

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void editProfile_ShouldUpdateSuccessfully_WhenInputValidWithSomeNulls() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "name": "Test User",
                "location": null,
                "bio": null,
                "isPrivate": false
            }
        """;

        mockMvc.perform(put("/api/users/edit-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));

        User updatedUser = userRepository.findByEmail(user.getEmail()).get();

        assertEquals(user.getName(), updatedUser.getName());
        assertNull(updatedUser.getLocation());
        assertNull(updatedUser.getBio());
        assertFalse(updatedUser.isPrivate());

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void editProfile_ShouldFail_WhenIsPrivateIsNull() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "name": "new name",
                "location": "new location",
                "bio": "new bio",
                "isPrivate": null
            }
        """;

        mockMvc.perform(put("/api/users/edit-profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("IsPrivate is required"));

    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void changePassword_ShouldUpdatePassword_WhenCurrentPasswordIsCorrect() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "currentPassword": "12345678",
                "newPassword": "newPassword"
            }
        """;

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Password changed successfully"));

        User updatedUser = userRepository.findByEmail(user.getEmail()).get();

        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void changePassword_ShouldFail_WhenCurrentPasswordIsInCorrect() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "currentPassword": "currentPassword",
                "newPassword": "newPassword"
            }
        """;

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Current password is not correct"));

        User updatedUser = userRepository.findByEmail(user.getEmail()).get();

        assertTrue(passwordEncoder.matches("12345678", updatedUser.getPassword()));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void changePassword_ShouldFail_WhenNewPasswordIsShort() throws Exception {
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

        userRepository.save(user);

        String requestBody = """
            {
                "currentPassword": "12345678",
                "newPassword": "new"
            }
        """;

        mockMvc.perform(put("/api/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("New password must be at least 8 characters"));

        User updatedUser = userRepository.findByEmail(user.getEmail()).get();

        assertTrue(passwordEncoder.matches("12345678", updatedUser.getPassword()));
    }

}
