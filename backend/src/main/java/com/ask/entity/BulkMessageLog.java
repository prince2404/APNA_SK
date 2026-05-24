package com.ask.entity;

import com.ask.enums.BulkMessageStatus;
import com.ask.enums.MessageChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulk_message_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BulkMessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageChannel channel;

    @Column(name = "target_criteria", nullable = false)
    private String targetCriteria;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_count", nullable = false)
    private Integer sentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BulkMessageStatus status = BulkMessageStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
