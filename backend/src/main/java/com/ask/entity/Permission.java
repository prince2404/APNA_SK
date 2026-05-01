package com.ask.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an available permission in the system.
 * Each permission is a module+action combination (e.g., USERS + VIEW).
 */
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_permissions_module_action", columnNames = {"module", "action"})
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String module;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 255)
    private String description;
}
