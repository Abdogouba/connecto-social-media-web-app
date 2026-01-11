package com.socialmedia.connecto.integrationtests;

import com.jayway.jsonpath.JsonPath;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest // loads the full application context
@AutoConfigureMockMvc // enables MockMvc auto-configuration
@ActiveProfiles("test") // use application-test.properties
public class AuthenticationControllerTests {

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
    void register_shouldCreateUser_WhenValidInput() throws Exception {

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "abdullah@example.com",
                "password": "mypassword123",
                "gender": "MALE",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding"
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
        assertNull(user.get().getBirthDate());
        assertEquals(Role.USER, user.get().getRole());
        assertTrue(passwordEncoder.matches("mypassword123", user.get().getPassword()));
        assertFalse(user.get().isPrivate());
        assertFalse(user.get().isBanned());

    }

    @Test
    void register_ShouldFail_WhenEmailAlreadyExists() throws Exception {
        User user = new User();
        user.setEmail("duplicate@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        String requestBody = """
            {
                "name": "Abdullah",
                "email": "duplicate@example.com",
                "password": "mypassword123",
                "gender": "MALE",
                "birthDate": "2002-04-11",
                "location": "New Cairo, Cairo, Egypt",
                "bio": "just coding"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
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
                "bio": "just coding"
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
                "bio": "just coding"
            }
        """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name is required"));

        assertEquals(0, userRepository.count());
    }

    @Test
    void login_ShouldReturnResponseDTO_WhenValidInput() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        User savedUser = userRepository.findByEmail(user.getEmail()).get();

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "12345678"
            }
        """;

        String responseJson = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token").exists())
                        .andExpect(jsonPath("$.id").value(savedUser.getId()))
                        .andExpect(jsonPath("$.name").value(user.getName()))
                        .andExpect(jsonPath("$.email").value(user.getEmail()))
                        .andExpect(jsonPath("$.gender").value(user.getGender().name()))
                        .andExpect(jsonPath("$.birthDate").value(user.getBirthDate()))
                        .andExpect(jsonPath("$.location").value(user.getLocation()))
                        .andExpect(jsonPath("$.bio").value(user.getBio()))
                        .andExpect(jsonPath("$.private").value((Boolean)false))
                        .andExpect(jsonPath("$.role").value(user.getRole().name()))
                        .andExpect(jsonPath("$.createdAt").exists())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token = JsonPath.read(responseJson, "$.token");

        assertEquals(user.getEmail(), jwtUtil.extractUsername(token));
        assertEquals(user.getRole().name(), jwtUtil.extractRole(token));

    }

    @Test
    void login_ShouldFail_WhenEmailNotFound() throws Exception {

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "ahlyplayer"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));

    }

    @Test
    void login_ShouldFail_WhenInvalidPassword() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "ahlyplayer"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));

    }

    @Test
    void login_ShouldFail_WhenUserIsBanned() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(true);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "12345678"
            }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is currently banned from the platform"));

    }

    @Test
    void userRoute_ShouldFail_WhenUserIsBanned() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        User savedUser = userRepository.findByEmail(user.getEmail()).get();
        savedUser.setBanned(true);

        userRepository.save(savedUser);

        mockMvc.perform(get("/api/users/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User is currently banned from the platform"));

    }

    @Test
    void userRoute_ShouldFail_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userRoute_ShouldWork_WithUserToken() throws Exception {
        mockMvc.perform(get("/api/users/test")
                        .header("Authorization", "Bearer " + generateToken(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(content().string("User area"));
    }

    private String generateToken(Role role) {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(role);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @Test
    void adminRoute_ShouldFail_WhenUserRole() throws Exception {
        mockMvc.perform(get("/api/admins/test")
                        .header("Authorization", "Bearer " + generateToken(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoute_ShouldWork_WhenAdmin() throws Exception {
        mockMvc.perform(get("/api/admins/test")
                        .header("Authorization", "Bearer " + generateToken(Role.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin area"));
    }

    @Test
    void adminRoute_ShouldWork_WhenSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/admins/test")
                        .header("Authorization", "Bearer " + generateToken(Role.SUPER_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin area"));
    }

    @Test
    void superRoute_ShouldWork_WhenSuperAdmin() throws Exception {
        mockMvc.perform(get("/api/supers/test")
                        .header("Authorization", "Bearer " + generateToken(Role.SUPER_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(content().string("Super area"));
    }

    @Test
    void superRoute_ShouldFail_WhenUserRole() throws Exception {
        mockMvc.perform(get("/api/supers/test")
                        .header("Authorization", "Bearer " + generateToken(Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void superRoute_ShouldFail_WhenAdminRole() throws Exception {
        mockMvc.perform(get("/api/supers/test")
                        .header("Authorization", "Bearer " + generateToken(Role.ADMIN)))
                .andExpect(status().isForbidden());
    }

    @Test
    void forgotPassword_ShouldChangePassword_WhenEmailValid() throws Exception {
        User user = new User();
        user.setEmail("abdalla.mahgoub@student.guc.edu.eg");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", user.getEmail()))
                .andExpect(status().isOk())
                .andExpect(content().string("Temporary password sent to your email, if email is valid."));

        Optional<User> updatedUser = userRepository.findByEmail(user.getEmail());

        assertTrue(updatedUser.isPresent());
        assertNotEquals(user.getPassword(), updatedUser.get().getPassword());
    }

    @Test
    void forgotPassword_ShouldReturn200Ok_WhenEmailDoesntExist() throws Exception {
        User user = new User();
        user.setEmail("abdalla.mahgoub@student.guc.edu.eg");
        user.setPassword(passwordEncoder.encode("12345678"));
        user.setRole(Role.USER);
        user.setName("Test User");
        user.setBanned(false);
        user.setGender(Gender.MALE);
        user.setPrivate(false);

        userRepository.save(user);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .param("email", "example@gouba.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("Temporary password sent to your email, if email is valid."));

        assertEquals(1, userRepository.count());
    }

}
