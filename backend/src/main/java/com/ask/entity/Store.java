package com.ask.entity;

import com.ask.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a physical ASK store/centre location.
 * Bottom level of the geographic hierarchy. Receptionists and Volunteers are assigned here.
 */
@Entity
@Table(name = "stores")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String phone;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id", nullable = false)
    private Block block;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
