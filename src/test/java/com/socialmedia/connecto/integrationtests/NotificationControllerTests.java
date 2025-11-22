package com.socialmedia.connecto.integrationtests;

import com.socialmedia.connecto.models.*;
import com.socialmedia.connecto.repositories.NotificationRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User receiver;
    private User sender;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        // Create receiver
        receiver = new User();
        receiver.setEmail("receiver@example.com");
        receiver.setPassword(passwordEncoder.encode("password"));
        receiver.setName("Receiver");
        receiver.setRole(Role.USER);
        receiver.setGender(Gender.MALE);
        receiver.setPrivate(false);
        receiver.setBanned(false);
        receiver.setBirthDate(LocalDate.of(2000, 1, 1));
        receiver = userRepository.save(receiver);

        // Create sender
        sender = new User();
        sender.setEmail("sender@example.com");
        sender.setPassword(passwordEncoder.encode("password"));
        sender.setName("Sender");
        sender.setRole(Role.USER);
        sender.setGender(Gender.MALE);
        sender.setPrivate(false);
        sender.setBanned(false);
        sender.setBirthDate(LocalDate.of(2000, 1, 1));
        sender = userRepository.save(sender);
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void getMyNotifications_ShouldReturnNotifications_WhenTheyExist() throws Exception {
        Notification notification1 = new Notification();
        notification1.setReceiver(receiver);
        notification1.setSender(sender);
        notification1.setType(NotificationType.COMMENT);
        notification1.setReferenceId(101L);
        notification1.setRead(false);
        notificationRepository.save(notification1);

        try {
            Thread.sleep(1000); // wait 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Notification notification2 = new Notification();
        notification2.setReceiver(receiver);
        notification2.setSender(sender);
        notification2.setType(NotificationType.NEW_FOLLOWER);
        notification2.setReferenceId(102L);
        notification2.setRead(false);
        notificationRepository.save(notification2);

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].senderId").value(sender.getId()))
                .andExpect(jsonPath("$.list[0].senderName").value(sender.getName()))
                .andExpect(jsonPath("$.list[0].type").value(notification2.getType().name()))
                .andExpect(jsonPath("$.list[0].referenceId").value(notification2.getReferenceId()))
                .andExpect(jsonPath("$.list[0].read").value(notification2.isRead()))
                .andExpect(jsonPath("$.list[0].createdAt").exists())
                .andExpect(jsonPath("$.list[1].senderId").value(sender.getId()))
                .andExpect(jsonPath("$.list[1].senderName").value(sender.getName()))
                .andExpect(jsonPath("$.list[1].type").value(notification1.getType().name()))
                .andExpect(jsonPath("$.list[1].referenceId").value(notification1.getReferenceId()))
                .andExpect(jsonPath("$.list[1].read").value(notification1.isRead()))
                .andExpect(jsonPath("$.list[1].createdAt").exists())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void getMyNotifications_ShouldReturnEmpty_WhenNoNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list.length()").value(0))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void countUnreadNotifications_ShouldReturnCorrectCount_WhenThereIsUnreadNotifications() throws Exception {

        Notification notification1 = new Notification();
        notification1.setReceiver(receiver);
        notification1.setSender(sender);
        notification1.setType(NotificationType.COMMENT);
        notification1.setReferenceId(101L);
        notification1.setRead(false);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setReceiver(receiver);
        notification2.setSender(sender);
        notification2.setType(NotificationType.COMMENT);
        notification2.setReferenceId(10L);
        notification2.setRead(false);
        notificationRepository.save(notification2);


        Notification notification3 = new Notification();
        notification3.setReceiver(receiver);
        notification3.setSender(sender);
        notification3.setType(NotificationType.COMMENT);
        notification3.setReferenceId(11L);
        notification3.setRead(true);
        notificationRepository.save(notification3);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void countUnreadNotifications_ShouldReturnZero_WhenThereIsNoUnreadNotifications() throws Exception {

        Notification notification1 = new Notification();
        notification1.setReceiver(receiver);
        notification1.setSender(sender);
        notification1.setType(NotificationType.COMMENT);
        notification1.setReferenceId(101L);
        notification1.setRead(true);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setReceiver(receiver);
        notification2.setSender(sender);
        notification2.setType(NotificationType.COMMENT);
        notification2.setReferenceId(10L);
        notification2.setRead(true);
        notificationRepository.save(notification2);

        Notification notification3 = new Notification();
        notification3.setReceiver(receiver);
        notification3.setSender(sender);
        notification3.setType(NotificationType.COMMENT);
        notification3.setReferenceId(11L);
        notification3.setRead(true);
        notificationRepository.save(notification3);

        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void countUnreadNotifications_ShouldReturnZero_WhenThereIsNoNotifications() throws Exception {
        mockMvc.perform(get("/api/notifications/unread-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void markMyNotificationsAsRead_ShouldReturn200Ok_WhenThereIsNoNotifications() throws Exception {
        mockMvc.perform(patch("/api/notifications/mark-all-read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Marked all notifications of user as read"));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void markMyNotificationsAsRead_ShouldReturn200Ok_WhenThereIsNoUnreadNotifications() throws Exception {
        Notification notification1 = new Notification();
        notification1.setReceiver(receiver);
        notification1.setSender(sender);
        notification1.setType(NotificationType.COMMENT);
        notification1.setReferenceId(101L);
        notification1.setRead(true);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setReceiver(receiver);
        notification2.setSender(sender);
        notification2.setType(NotificationType.COMMENT);
        notification2.setReferenceId(10L);
        notification2.setRead(true);
        notificationRepository.save(notification2);

        mockMvc.perform(patch("/api/notifications/mark-all-read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Marked all notifications of user as read"));
    }

    @Test
    @WithMockUser(username = "receiver@example.com", roles = {"USER"})
    void markMyNotificationsAsRead_ShouldUpdateNotifications_WhenThereIsUnreadNotifications() throws Exception {
        Notification notification1 = new Notification();
        notification1.setReceiver(receiver);
        notification1.setSender(sender);
        notification1.setType(NotificationType.COMMENT);
        notification1.setReferenceId(101L);
        notification1.setRead(false);
        notificationRepository.save(notification1);

        Notification notification2 = new Notification();
        notification2.setReceiver(receiver);
        notification2.setSender(sender);
        notification2.setType(NotificationType.COMMENT);
        notification2.setReferenceId(10L);
        notification2.setRead(false);
        notificationRepository.save(notification2);

        Notification notification3 = new Notification();
        notification3.setReceiver(receiver);
        notification3.setSender(sender);
        notification3.setType(NotificationType.COMMENT);
        notification3.setReferenceId(11L);
        notification3.setRead(true);
        notificationRepository.save(notification3);

        mockMvc.perform(patch("/api/notifications/mark-all-read")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Marked all notifications of user as read"));

        long unreadCount = notificationRepository.countByReceiverAndIsReadFalse(this.receiver);
        assertEquals(0, unreadCount);
    }

}
