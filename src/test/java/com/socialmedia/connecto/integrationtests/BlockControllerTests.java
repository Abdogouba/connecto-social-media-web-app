package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.BlockRepository;
import com.socialmedia.connecto.repositories.FollowRepository;
import com.socialmedia.connecto.repositories.FollowRequestRepository;
import com.socialmedia.connecto.repositories.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BlockControllerTests {

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

    private User blocker;
    private User blocked;

    @BeforeEach
    void setUp() {
        followRequestRepository.deleteAll();
        followRepository.deleteAll();
        blockRepository.deleteAll();
        userRepository.deleteAll();

        // Create blocker
        blocker = new User();
        blocker.setEmail("blocker@example.com");
        blocker.setPassword(passwordEncoder.encode("password"));
        blocker.setName("blocker");
        blocker.setRole(Role.USER);
        blocker.setGender(Gender.MALE);
        blocker.setPrivate(false);
        blocker.setBanned(false);
        blocker.setBirthDate(LocalDate.of(2000, 1, 1));
        blocker = userRepository.save(blocker);

        // Create blocked
        blocked = new User();
        blocked.setEmail("blocked@example.com");
        blocked.setPassword(passwordEncoder.encode("password"));
        blocked.setName("blocked");
        blocked.setRole(Role.USER);
        blocked.setGender(Gender.MALE);
        blocked.setPrivate(false);
        blocked.setBanned(false);
        blocked.setBirthDate(LocalDate.of(2000, 1, 1));
        blocked = userRepository.save(blocked);

        Follow f1 = new Follow();
        f1.setFollower(blocker);
        f1.setFollowed(blocked);
        followRepository.save(f1);

        Follow f2 = new Follow();
        f2.setFollower(blocked);
        f2.setFollowed(blocker);
        followRepository.save(f2);
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldReturn400BadRequest_WhenUserBlocksHimself() throws Exception {
        mockMvc.perform(post("/api/blocks/" + blocker.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User cannot block himself"));

        assertEquals(0, blockRepository.count());
        assertEquals(2, followRepository.count());
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldReturn404NotFound_WhenToBeBlockedNotFound() throws Exception {
        mockMvc.perform(post("/api/blocks/" + 11111)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User to be blocked not found"));

        assertEquals(0, blockRepository.count());
        assertEquals(2, followRepository.count());
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldReturn403Forbidden_WhenToBeBlockedIsAdmin() throws Exception {
        blocked.setRole(Role.ADMIN);
        blocked = userRepository.save(blocked);

        mockMvc.perform(post("/api/blocks/" + blocked.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User cannot block admins"));

        assertEquals(0, blockRepository.count());
        assertEquals(2, followRepository.count());
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldReturn409Conflict_WhenBlockAlreadyExists() throws Exception {
        createAndSaveBlock();

        mockMvc.perform(post("/api/blocks/" + blocked.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().string("This user is already blocked"));

        assertEquals(1, blockRepository.count());
    }

    private void createAndSaveBlock() {
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        blockRepository.save(block);
        followRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldCreateBlock_WhenValid() throws Exception {
        mockMvc.perform(post("/api/blocks/" + blocked.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User blocked successfully"));

        boolean blockExists = blockRepository.existsByBlockerAndBlocked(blocker, blocked);

        assertTrue(blockExists);
        assertEquals(0, followRepository.count());
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void unblock_ShouldDeleteBlock_WhenValidBlockedUserId() throws Exception {
        createAndSaveBlock();

        mockMvc.perform(delete("/api/blocks/" + blocked.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(0, blockRepository.count());
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void unblock_ShouldReturn204NoContent_WhenInValidBlockedUserId() throws Exception {
        createAndSaveBlock();

        mockMvc.perform(delete("/api/blocks/" + 11111)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(1, blockRepository.count());
    }

}
