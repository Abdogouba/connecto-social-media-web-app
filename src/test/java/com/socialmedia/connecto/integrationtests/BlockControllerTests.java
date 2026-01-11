package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.enums.Gender;
import com.socialmedia.connecto.enums.Role;
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

    private Block createAndSaveBlock() {
        Block block = new Block();
        block.setBlocker(blocker);
        block.setBlocked(blocked);
        block = blockRepository.save(block);
        followRepository.deleteAll();
        return block;
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    public void block_ShouldCreateBlock_WhenValid() throws Exception {
        mockMvc.perform(post("/api/blocks/" + blocked.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("User blocked successfully"));

        boolean blockExists = blockRepository.existsByBlockerIdAndBlockedId(blocker.getId(), blocked.getId());

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

    @Test
    @WithMockUser(username = "blocker@example.com")
    void getBlockedUsers_ShouldReturnBlockedUsersPaginated_WhenTheyExist() throws Exception {
        Block b1 = createAndSaveBlock();

        User blocked2 = new User();
        blocked2.setEmail("blocked2@example.com");
        blocked2.setPassword(passwordEncoder.encode("password"));
        blocked2.setName("blocked2");
        blocked2.setRole(Role.USER);
        blocked2.setGender(Gender.MALE);
        blocked2.setPrivate(false);
        blocked2.setBanned(false);
        blocked2.setBirthDate(LocalDate.of(2000, 1, 1));
        blocked2 = userRepository.save(blocked2);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Block b2 = new Block();
        b2.setBlocker(blocker);
        b2.setBlocked(blocked2);
        b2 = blockRepository.save(b2);

        mockMvc.perform(get("/api/blocks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].id").value(blocked2.getId()))
                .andExpect(jsonPath("$.list[0].name").value(blocked2.getName()))
                .andExpect(jsonPath("$.list[0].blockedAt").exists())
                .andExpect(jsonPath("$.list[1].id").value(blocked.getId()))
                .andExpect(jsonPath("$.list[1].name").value(blocked.getName()))
                .andExpect(jsonPath("$.list[1].blockedAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "blocker@example.com")
    void getBlockedUsers_ShouldReturnEmpty_WhenNoBlockedUsers() throws Exception {
        mockMvc.perform(get("/api/blocks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

}
