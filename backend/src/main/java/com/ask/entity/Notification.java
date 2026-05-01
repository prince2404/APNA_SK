package com.ask.entity;

import com.ask.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * In-app notification for a user.
 * Created by the system when important events occur (permission request submitted/reviewed,
 * KYC verification status changed, etc.).
 */
@Entity
@Table(name = "notifications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** Whether the user has read this notification */
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    /** Optional reference to a related entity (e.g., permission_request.id) */
    @Column(name = "ref_entity_type", length = 50)
    private String refEntityType;

    @Column(name = "ref_entity_id")
    private Long refEntityId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
