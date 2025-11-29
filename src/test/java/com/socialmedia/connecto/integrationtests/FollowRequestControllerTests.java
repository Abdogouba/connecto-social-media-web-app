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
public class FollowRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @WithMockUser(username = "followed@example.com")
    void getFollowRequestsReceived_ShouldReturn400BadRequest_WhenUserIsPublic() throws Exception {
        mockMvc.perform(get("/api/follow-requests/received")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Public users do not have follow requests"));
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    void getFollowRequestsReceived_ShouldReturnRequestsPaginated_WhenUserIsPrivate() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        createAndSaveFollowRequest();

        User follower2 = new User();
        follower2.setEmail("follower2@example.com");
        follower2.setPassword(passwordEncoder.encode("password"));
        follower2.setName("follower2");
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

        FollowRequest request2 = new FollowRequest();
        request2.setFollower(follower2);
        request2.setFollowed(followed);
        followRequestRepository.save(request2);

        mockMvc.perform(get("/api/follow-requests/received")
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
    @WithMockUser(username = "followed@example.com")
    void getFollowRequestsReceived_ShouldReturnEmptyList_WhenUserPrivateAndHasNoRequests() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        mockMvc.perform(get("/api/follow-requests/received")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "followed@example.com")
    void getFollowRequestsReceived_ShouldReturnRequestsPaginated_WhenUserIsPrivateAnd2ndPage() throws Exception {
        followed.setPrivate(true);
        followed = userRepository.save(followed);

        createAndSaveFollowRequest();

        User follower2 = new User();
        follower2.setEmail("follower2@example.com");
        follower2.setPassword(passwordEncoder.encode("password"));
        follower2.setName("follower2");
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

        FollowRequest request2 = new FollowRequest();
        request2.setFollower(follower2);
        request2.setFollowed(followed);
        followRequestRepository.save(request2);

        mockMvc.perform(get("/api/follow-requests/received")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(1))
                .andExpect(jsonPath("$.list[0].id").value(follower.getId()))
                .andExpect(jsonPath("$.list[0].name").value(follower.getName()))
                .andExpect(jsonPath("$.list[0].followedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    private FollowRequest createAndSaveFollowRequest() {
        FollowRequest followRequest = new FollowRequest();
        followRequest.setFollower(follower);
        followRequest.setFollowed(followed);
        return followRequestRepository.save(followRequest);
    }

}

