package com.ask.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a role in the system with a hierarchy level.
 * 8 fixed roles: SUPER_ADMIN(1), SYSTEM_ADMIN(2), STATE_ADMIN(3), DISTRICT_ADMIN(4),
 * BLOCK_ADMIN(5), RECEPTIONIST(6), VOLUNTEER(7), PHARMACIST(3).
 */
@Entity
@Table(name = "roles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "hierarchy_level", nullable = false)
    private Integer hierarchyLevel;
}
